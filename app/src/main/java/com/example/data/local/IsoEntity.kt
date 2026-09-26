package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.IsoImage

@Entity(tableName = "iso_images")
data class IsoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val fileName: String,
    val distroId: String,
    val sizeMb: Int,
    val architecture: String,
    val isDownloaded: Boolean,
    val downloadProgressPercent: Int,
    val localUri: String,
    val sha256Checksum: String,
    val isCustomUserIso: Boolean
) {
    fun toDomain(): IsoImage = IsoImage(
        id = id,
        name = name,
        fileName = fileName,
        distroId = distroId,
        sizeMb = sizeMb,
        architecture = architecture,
        isDownloaded = isDownloaded,
        downloadProgressPercent = downloadProgressPercent,
        localUri = localUri,
        sha256Checksum = sha256Checksum,
        isCustomUserIso = isCustomUserIso
    )

    companion object {
        fun fromDomain(model: IsoImage): IsoEntity = IsoEntity(
            id = model.id,
            name = model.name,
            fileName = model.fileName,
            distroId = model.distroId,
            sizeMb = model.sizeMb,
            architecture = model.architecture,
            isDownloaded = model.isDownloaded,
            downloadProgressPercent = model.downloadProgressPercent,
            localUri = model.localUri,
            sha256Checksum = model.sha256Checksum,
            isCustomUserIso = model.isCustomUserIso
        )
    }
}
