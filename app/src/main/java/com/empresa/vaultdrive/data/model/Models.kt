package com.empresa.vaultdrive.data.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────
// RESPONSE LIST
// ─────────────────────────────────────────────
data class DriveItemListResponse(
    @SerializedName("value")
    val value: List<DriveItem> = emptyList()
)

// ─────────────────────────────────────────────
// DRIVE ITEM (RAW GRAPH)
// ─────────────────────────────────────────────
data class DriveItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("folder") val folder: FolderFacet? = null,
    @SerializedName("file") val file: FileFacet? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("lastModifiedDateTime") val lastModifiedDateTime: String? = null,
    @SerializedName("@microsoft.graph.downloadUrl") val downloadUrl: String? = null,
    @SerializedName("remoteItem") val remoteItem: RemoteItem? = null
) {
    val isFolder: Boolean
        get() = folder != null
}

// ─────────────────────────────────────────────
// FOLDER / FILE FACETS
// ─────────────────────────────────────────────
data class FolderFacet(
    @SerializedName("childCount")
    val childCount: Int = 0
)

data class FileFacet(
    @SerializedName("mimeType")
    val mimeType: String? = null
)

// ─────────────────────────────────────────────
// SHAREPOINT / SHARED ITEM INFO
// ─────────────────────────────────────────────
data class RemoteItem(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("parentReference")
    val parentReference: ParentRef? = null
)

data class ParentRef(
    @SerializedName("driveId")
    val driveId: String? = null
)

// ─────────────────────────────────────────────
// FILE ITEM (UI MODEL)
// ─────────────────────────────────────────────
data class FileItem(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val size: Long = 0L,
    val lastModified: String = "",
    val mimeType: String? = null,
    val downloadUrl: String? = null,
    val childCount: Int = 0,

    // 🔥 COMPARTIDOS / SHAREPOINT
    val isShared: Boolean = false,
    val driveId: String? = null,
    val remoteId: String? = null
)

// ─────────────────────────────────────────────
// MAPPER
// ─────────────────────────────────────────────
fun DriveItem.toFileItem() = FileItem(
    id = id,
    name = name,
    isFolder = isFolder,
    size = size ?: 0L,
    lastModified = lastModifiedDateTime ?: "",
    mimeType = file?.mimeType,
    downloadUrl = downloadUrl,
    childCount = folder?.childCount ?: 0,

    // 🔥 SHARE / REMOTE DETECTION
    isShared = remoteItem != null,

    // 🔥 NECESARIO PARA SHAREPOINT
    driveId = remoteItem?.parentReference?.driveId,
    remoteId = remoteItem?.id
)

// ─────────────────────────────────────────────
// RESULT WRAPPER
// ─────────────────────────────────────────────
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

// ─────────────────────────────────────────────
// CREATE FOLDER
// ─────────────────────────────────────────────
data class CreateFolderRequest(
    @SerializedName("name") val name: String,
    @SerializedName("folder") val folder: FolderFacet = FolderFacet(),
    @SerializedName("@microsoft.graph.conflictBehavior")
    val conflictBehavior: String = "fail"
)

// ─────────────────────────────────────────────
// UPLOAD SESSION
// ─────────────────────────────────────────────
data class UploadSessionRequest(
    @SerializedName("item")
    val item: UploadItemProps = UploadItemProps()
)

data class UploadItemProps(
    @SerializedName("@microsoft.graph.conflictBehavior")
    val conflictBehavior: String = "replace"
)

data class UploadSession(
    @SerializedName("uploadUrl")
    val uploadUrl: String = ""
)
