package com.empresa.vaultdrive.ui.browser

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.empresa.vaultdrive.R
import com.empresa.vaultdrive.core.security.Prefs
import com.empresa.vaultdrive.core.session.TokenManager
import com.empresa.vaultdrive.data.model.FileItem
import com.empresa.vaultdrive.data.model.Result
import com.empresa.vaultdrive.data.repository.Repository
import com.empresa.vaultdrive.databinding.ActivityBrowserBinding
import com.empresa.vaultdrive.ui.auth.AuthActivity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrowserActivity : AppCompatActivity() {

    private lateinit var b: ActivityBrowserBinding
    private lateinit var repo: Repository
    private lateinit var adapter: FileAdapter

    data class BreadcrumbEntry(val item: FileItem, val driveId: String?)

    private val breadcrumb = ArrayDeque<BreadcrumbEntry>()
    private var currentFolder: FileItem? = null
    private var currentDriveId: String? = null

    private var pendingCameraUri: Uri? = null
    private var pendingCameraFile: File? = null

    // ── Pick files ─────────────────────────────────────────────────────
    private val pickLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                val uris =
                    data.clipData?.let { c ->
                        (0 until c.itemCount).map { c.getItemAt(it).uri }
                    } ?: listOfNotNull(data.data)

                if (uris.size == 1) askRenameAndUpload(uris[0])
                else uris.forEach { uploadFile(it, null) }
            }
        }

    // ── Camera ────────────────────────────────────────────────────────
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val uri = pendingCameraUri ?: return@registerForActivityResult

                val defaultName =
                    "Foto_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"

                askRenameBeforeUpload(uri, defaultName)
            }

            pendingCameraFile?.delete()
            pendingCameraFile = null
        }

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
            if (res.values.all { it }) openPicker()
            else snack("Se necesitan permisos")
        }

    private val cameraPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else snack("Permiso de cámara requerido")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(b.root)

        repo = Repository(this)

        setupRecycler()
        setupButtons()

        updatePinnedCard()
        loadFolder("root", null)
    }

    // ────────────────────────────────────────────────────────────────
    // Recycler
    private fun setupRecycler() {
        adapter = FileAdapter(
            onClick = { item ->
                if (item.isFolder) {
                    val driveId = if (item.isShared) extractDriveId(item) else currentDriveId
                    openFolder(item, driveId)
                }
            },
            onLongClick = { showItemMenu(it) }
        )

        b.rvFiles.layoutManager = LinearLayoutManager(this)
        b.rvFiles.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { refresh() }
    }

    private fun setupButtons() {
        b.btnBack.setOnClickListener { navigateUp() }
        b.fabCamera.setOnClickListener { checkCamera() }
        b.fabUpload.setOnClickListener { checkPermAndPick() }
        b.fabNewFolder.setOnClickListener { showCreateFolderDialog() }

        b.btnSignOut.setOnClickListener { confirmSignOut() }
        b.btnShared.setOnClickListener { loadSharedFolders() }
    }

    // ────────────────────────────────────────────────────────────────
    // CAMERA FIX REAL (ESTO era tu problema)
    private fun checkCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val stamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val file = File.createTempFile("cam_$stamp", ".jpg", cacheDir)
            pendingCameraFile = file

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            pendingCameraUri = uri
            cameraLauncher.launch(uri)

        } catch (e: Exception) {
            snack("Error cámara: ${e.message}")
            e.printStackTrace()
        }
    }

    // ────────────────────────────────────────────────────────────────
    // Upload dialogs
    private fun askRenameBeforeUpload(uri: Uri, defaultName: String) {
        val base = defaultName.substringBeforeLast('.')

        val et = EditText(this).apply {
            setText(base)
        }

        AlertDialog.Builder(this)
            .setTitle("Nombre foto")
            .setView(et)
            .setPositiveButton("Subir") { _, _ ->
                uploadFile(uri, "${et.text}.jpg")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askRenameAndUpload(uri: Uri) {
        val name = getFileName(uri)
        val base = name.substringBeforeLast('.')
        val ext = if (name.contains('.')) ".${name.substringAfterLast('.')}" else ""

        val et = EditText(this).apply {
            setText(base)
        }

        AlertDialog.Builder(this)
            .setTitle("Renombrar archivo")
            .setView(et)
            .setPositiveButton("Subir") { _, _ ->
                uploadFile(uri, et.text.toString() + ext)
            }
            .setNegativeButton("Subir sin renombrar") { _, _ ->
                uploadFile(uri, null)
            }
            .show()
    }

    // ────────────────────────────────────────────────────────────────
    // Upload
    private fun uploadFile(uri: Uri, customName: String?) {
        lifecycleScope.launch {
            val token = getToken() ?: return@launch

            val parentId =
                currentFolder?.id ?: Prefs.pinnedFolderId.ifBlank { "root" }

            when (val r = repo.uploadFile(token, parentId, uri, customName)) {
                is Result.Success -> snack("Subido ✓")
                is Result.Error -> snack(r.message)
            }
        }
    }

    // ────────────────────────────────────────────────────────────────
    private fun getFileName(uri: Uri): String {
        var name = "file"

        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && idx >= 0) {
                name = c.getString(idx)
            }
        }

        return name
    }

    private fun snack(msg: String) =
        Snackbar.make(b.root, msg, Snackbar.LENGTH_LONG).show()

    private suspend fun getToken(): String? {
        if (Prefs.isTokenValid()) return Prefs.token
        return TokenManager.refreshSilently()
    }

    // ────────────────────────────────────────────────────────────────
    // TODO: deja tus métodos tal cual (no los toqué)
    private fun loadFolder(id: String, driveId: String?) {}
    private fun refresh() {}
    private fun openFolder(folder: FileItem, driveId: String?) {}
    private fun navigateUp() {}
    private fun loadSharedFolders() {}
    private fun updatePinnedCard() {}
    private fun showItemMenu(item: FileItem) {}
    private fun showCreateFolderDialog() {}
    private fun confirmSignOut() {}
    private fun checkPermAndPick() {}
    private fun openPicker() {}
    private fun extractDriveId(item: FileItem): String? = null
}
