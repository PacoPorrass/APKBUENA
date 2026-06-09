package com.empresa.vaultdrive.data.api

import com.empresa.vaultdrive.data.model.*
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface OneDriveApi {

    // ─────────────────────────────────────────────
    // ROOT (OneDrive personal)
    // ─────────────────────────────────────────────
    @GET("v1.0/me/drive/root/children")
    suspend fun getRootChildren(
        @Header("Authorization") token: String,
        @Query("\$select") select: String =
            "id,name,folder,file,size,lastModifiedDateTime,remoteItem,parentReference,@microsoft.graph.downloadUrl",
        @Query("\$orderby") order: String = "name asc"
    ): Response<DriveItemListResponse>

    // ─────────────────────────────────────────────
    // CHILDREN (OneDrive personal)
    // ─────────────────────────────────────────────
    @GET("v1.0/me/drive/items/{itemId}/children")
    suspend fun getChildren(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Query("\$select") select: String =
            "id,name,folder,file,size,lastModifiedDateTime,remoteItem,parentReference,@microsoft.graph.downloadUrl",
        @Query("\$orderby") order: String = "name asc"
    ): Response<DriveItemListResponse>

    // ─────────────────────────────────────────────
    // SHARED WITH ME
    // ─────────────────────────────────────────────
    @GET("v1.0/me/drive/sharedWithMe")
    suspend fun getSharedWithMe(
        @Header("Authorization") token: String,
        @Query("\$select") select: String =
            "id,name,remoteItem,parentReference",
        @Query("\$expand") expand: String =
            "remoteItem"
    ): Response<DriveItemListResponse>

    // ─────────────────────────────────────────────
    // SHARE LINK → CONVERTIR URL EXTERNA (IMPORTANTE)
    // ─────────────────────────────────────────────
    @GET("v1.0/shares/{shareId}/driveItem")
    suspend fun getDriveItemFromShare(
        @Header("Authorization") token: String,
        @Path("shareId") shareId: String
    ): Response<DriveItem>

    // ─────────────────────────────────────────────
    // DRIVE EXTERNO (SharePoint / shared drives)
    // ─────────────────────────────────────────────
    @GET("v1.0/drives/{driveId}/items/{itemId}/children")
    suspend fun getChildrenByDrive(
        @Header("Authorization") token: String,
        @Path("driveId") driveId: String,
        @Path("itemId") itemId: String,
        @Query("\$select") select: String =
            "id,name,folder,file,size,lastModifiedDateTime,remoteItem,parentReference,@microsoft.graph.downloadUrl",
        @Query("\$orderby") order: String = "name asc"
    ): Response<DriveItemListResponse>

    // ─────────────────────────────────────────────
    // CREAR CARPETA
    // ─────────────────────────────────────────────
    @POST("v1.0/me/drive/items/{parentId}/children")
    suspend fun createFolder(
        @Header("Authorization") token: String,
        @Path("parentId") parentId: String,
        @Body body: CreateFolderRequest
    ): Response<DriveItem>

    // ─────────────────────────────────────────────
    // UPLOAD (OneDrive personal)
    // ─────────────────────────────────────────────
    @PUT("v1.0/me/drive/items/{parentId}:/{fileName}:/content")
    suspend fun uploadSmall(
        @Header("Authorization") token: String,
        @Path("parentId") parentId: String,
        @Path("fileName", encoded = false) fileName: String,
        @Body content: RequestBody
    ): Response<DriveItem>

    // ─────────────────────────────────────────────
    // UPLOAD (DRIVE COMPARTIDO)
    // ─────────────────────────────────────────────
    @PUT("v1.0/drives/{driveId}/items/{parentId}:/{fileName}:/content")
    suspend fun uploadSmallShared(
        @Header("Authorization") token: String,
        @Path("driveId") driveId: String,
        @Path("parentId") parentId: String,
        @Path("fileName", encoded = false) fileName: String,
        @Body content: RequestBody
    ): Response<DriveItem>

    // ─────────────────────────────────────────────
    // UPLOAD SESSION (OneDrive)
    // ─────────────────────────────────────────────
    @POST("v1.0/me/drive/items/{parentId}:/{fileName}:/createUploadSession")
    suspend fun createUploadSession(
        @Header("Authorization") token: String,
        @Path("parentId") parentId: String,
        @Path("fileName", encoded = false) fileName: String,
        @Body body: UploadSessionRequest
    ): Response<UploadSession>

    // ─────────────────────────────────────────────
    // UPLOAD SESSION (SHARED DRIVE)
    // ─────────────────────────────────────────────
    @POST("v1.0/drives/{driveId}/items/{parentId}:/{fileName}:/createUploadSession")
    suspend fun createUploadSessionShared(
        @Header("Authorization") token: String,
        @Path("driveId") driveId: String,
        @Path("parentId") parentId: String,
        @Path("fileName", encoded = false) fileName: String,
        @Body body: UploadSessionRequest
    ): Response<UploadSession>

    // ─────────────────────────────────────────────
    // FACTORY
    // ─────────────────────────────────────────────
    companion object {

        fun create(): OneDriveApi {

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://graph.microsoft.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OneDriveApi::class.java)
        }
    }
}
