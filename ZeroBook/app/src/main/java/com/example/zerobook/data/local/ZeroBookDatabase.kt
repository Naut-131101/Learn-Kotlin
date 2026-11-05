package com.example.zerobook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ShelfBookEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ZeroBookDatabase : RoomDatabase() {
    abstract fun shelfBookDao(): ShelfBookDao
}
