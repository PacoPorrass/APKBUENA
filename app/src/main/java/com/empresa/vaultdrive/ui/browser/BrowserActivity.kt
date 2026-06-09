package com.empresa.vaultdrive.ui.browser

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.empresa.vaultdrive.core.security.Prefs
import com.empresa.vaultdrive.core.session.TokenManager
import com.empresa.vaultdrive.data.model.FileItem
import com.empresa.vaultdrive.data.model.Result
import com.empresa.vaultdrive.data.repository.Repository
import com.empresa.vaultdrive.databinding.ActivityBrowserBinding
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

    // ─────────────────────────────
    // PICK FILES
    // ─────────────────────────────
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

    // ─────────────────────────────
    // CAMERA
    // ─────────────────────────────
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

    // ─────────────────────────────
    // RECYCLER
    // ─────────────────────────────
    private fun setupRecycler() {
        adapter = FileAdapter(
            onClick = { item ->
                if (item.isFolder) {

                    val driveId =
                        item.driveId ?: currentDriveId

                    openFolder(item, driveId)
                }
            },
            onLongClick = { showItemMenu(it) }
        )

        b.rvFiles.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
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

    // ─────────────────────────────
    // CAMERA
    // ─────────────────────────────
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
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val file = File.createTempFile("cam_$stamp", ".jpg", cacheDir)
        pendingCameraFile = file

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    // ─────────────────────────────
    // UPLOAD
    // ─────────────────────────────
    private fun uploadFile(uri: Uri, customName: String?) {
        lifecycleScope.launch {

            val token = getToken() ?: return@launch

            val parentId =
                currentFolder?.id
                    ?: Prefs.pinnedFolderId.ifBlank { "root" }

            when (val r = repo.uploadFile(token, parentId, uri, customName)) {
                is Result.Success -> snack("Subido ✓")
                is Result.Error -> snack(r.message)
            }
        }
    }

    private suspend fun getToken(): String? {
        if (Prefs.isTokenValid()) return Prefs.token
        return TokenManager.refreshSilently()
    }

    // ─────────────────────────────
    // FILE NAME
    // ─────────────────────────────
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

    // ─────────────────────────────
    // PLACEHOLDERS (sin tocar lógica)
    // ─────────────────────────────
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
}
