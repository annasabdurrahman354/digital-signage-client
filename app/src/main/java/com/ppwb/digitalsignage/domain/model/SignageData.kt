package com.ppwb.digitalsignage.domain.model

data class SignageData(
    val version: String,
    val device: Device,
    val schedules: List<Schedule>,
    val playlists: List<Playlist>,
    val assets: List<Asset>
)

data class Device(
    val id: String,
    val deviceName: String,
    val serialNumber: String,
    val location: String,
    val defaultPlaylistId: String
)

data class Schedule(
    val id: String,
    val playlistId: String,
    val priority: Int,
    val validFromDate: String,
    val validUntilDate: String,
    val timeStart: String,
    val timeEnd: String,
    val daysOfWeek: List<Int>
)

data class Playlist(
    val id: String,
    val name: String,
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val assetId: String,
    val order: Int,
    val durationSec: Int
)

data class Asset(
    val id: String,
    val name: String,
    val type: String, // "video" or "image"
    val fileUrl: String,
    val hashMd5: String,
    val sizeBytes: Long,
    val localPath: String? = null
)
