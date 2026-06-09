package com.empresa.vaultdrive.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object Prefs {

    private const val FILE = "vd_prefs_v2"
    private var sp: SharedPreferences? = null

    fun init(context: Context) {
        sp = try {
            val key = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedSharedPreferences.create(
                FILE,
                key,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

        } catch (e: Exception) {
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        }
    }

    private fun p() = sp!!

    // ─────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────
    var token: String?
        get() = p().getString("token", null)
        set(v) = p().edit().putString("token", v).apply()

    var tokenExpiry: Long
        get() = p().getLong("token_exp", 0L)
        set(v) = p().edit().putLong("token_exp", v).apply()

    var userName: String
        get() = p().getString("user_name", "") ?: ""
        set(v) = p().edit().putString("user_name", v).apply()

    // ─────────────────────────────────────────────
    // PINNED FOLDER
    // ─────────────────────────────────────────────
    var pinnedFolderId: String
        get() = p().getString("pinned_id", "") ?: ""
        set(v) = p().edit().putString("pinned_id", v).apply()

    var pinnedFolderName: String
        get() = p().getString("pinned_name", "") ?: ""
        set(v) = p().edit().putString("pinned_name", v).apply()

    var pinnedDriveId: String
        get() = p().getString("pinned_drive", "") ?: ""
        set(v) = p().edit().putString("pinned_drive", v).apply()

    // 🔥 NUEVO: indica si la carpeta fijada es compartida / SharePoint
    var pinnedIsShared: Boolean
        get() = p().getBoolean("pinned_shared", false)
        set(v) = p().edit().putBoolean("pinned_shared", v).apply()

    // 🔥 NUEVO: remoteItemId (útil para SharePoint navegación avanzada)
    var pinnedRemoteId: String
        get() = p().getString("pinned_remote_id", "") ?: ""
        set(v) = p().edit().putString("pinned_remote_id", v).apply()

    // ─────────────────────────────────────────────
    // VALIDACIÓN TOKEN
    // ─────────────────────────────────────────────
    fun isTokenValid(): Boolean {
        val t = token ?: return false
        return t.isNotBlank() &&
                tokenExpiry > System.currentTimeMillis() + 5 * 60 * 1000
    }

    fun clear() = p().edit().clear().apply()
}
