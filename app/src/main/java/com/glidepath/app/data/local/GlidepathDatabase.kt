package com.glidepath.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GoalEntity::class, PaymentEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GlidepathDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        const val NAME = "glidepath.db"
    }
}
