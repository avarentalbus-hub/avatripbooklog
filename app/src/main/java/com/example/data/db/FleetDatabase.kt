package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    DriverEntity::class,
    TripEntity::class,
    ExtraTripEntity::class,
    ExpenseEntity::class,
    BusEntity::class,
    QueueEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class FleetDatabase : RoomDatabase() {
  abstract fun fleetDao(): FleetDao

  companion object {
    @Volatile
    private var INSTANCE: FleetDatabase? = null

    fun getDatabase(context: Context): FleetDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          FleetDatabase::class.java,
          "fleetshift_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
