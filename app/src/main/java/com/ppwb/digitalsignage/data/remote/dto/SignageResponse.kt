package com.ppwb.digitalsignage.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SignageResponse(
    @SerializedName("version") val version: String,
    @SerializedName("device") val device: DeviceDto,
    @SerializedName("schedules") val schedules: List<ScheduleDto>,
    @SerializedName("playlists") val playlists: List<PlaylistDto>,
    @SerializedName("assets") val assets: List<AssetDto>
)

data class DeviceDto(
    @SerializedName("id") val id: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("serial_number") val serialNumber: String,
    @SerializedName("location") val location: String,
    @SerializedName("default_playlist_id") val defaultPlaylistId: String
)

data class ScheduleDto(
    @SerializedName("id") val id: String,
    @SerializedName("playlist_id") val playlistId: String,
    @SerializedName("priority") val priority: Int,
    @SerializedName("valid_from_date") val validFromDate: String,
    @SerializedName("valid_until_date") val validUntilDate: String,
    @SerializedName("time_start") val timeStart: String,
    @SerializedName("time_end") val timeEnd: String,
    @SerializedName("days_of_week") val daysOfWeek: List<Any> // Can be Int or String based on JSON
)

data class PlaylistDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("items") val items: List<PlaylistItemDto>
)

data class PlaylistItemDto(
    @SerializedName("asset_id") val assetId: String,
    @SerializedName("order") val order: Int,
    @SerializedName("duration_sec") val durationSec: Int
)

data class AssetDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("hash_md5") val hashMd5: String,
    @SerializedName("size_bytes") val sizeBytes: Long
)
