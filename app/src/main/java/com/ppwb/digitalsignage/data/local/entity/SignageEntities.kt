package com.ppwb.digitalsignage.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 1,
    val version: String
)

@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val deviceName: String,
    val serialNumber: String,
    val location: String,
    val defaultPlaylistId: String
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val priority: Int,
    val validFromDate: String,
    val validUntilDate: String,
    val timeStart: String,
    val timeEnd: String,
    val daysOfWeek: String // CSV of days
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: String,
    val assetId: String,
    val orderIndex: Int,
    val durationSec: Int
)

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val fileUrl: String,
    val hashMd5: String,
    val sizeBytes: Long,
    val localPath: String? = null
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val version: String,
    val jsonData: String // Full JSON response
)
