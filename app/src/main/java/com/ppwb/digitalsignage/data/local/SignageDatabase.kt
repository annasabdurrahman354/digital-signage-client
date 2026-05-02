package com.ppwb.digitalsignage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ppwb.digitalsignage.data.local.dao.SignageDao
import com.ppwb.digitalsignage.data.local.entity.*

@Database(
    entities = [
        ConfigEntity::class,
        DeviceEntity::class,
        ScheduleEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        AssetEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SignageDatabase : RoomDatabase() {
    abstract val signageDao: SignageDao
}
