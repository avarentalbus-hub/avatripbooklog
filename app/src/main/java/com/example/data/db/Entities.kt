package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class DriverEntity(
  @PrimaryKey val id: String,
  val name: String,
  val bus: String,
  val phone: String,
  val pin: String,
  val status: String
)

@Entity(tableName = "trips")
data class TripEntity(
  @PrimaryKey val id: String,
  val driverId: String,
  val driverName: String,
  val bus: String,
  val busSource: String,
  val assignedBus: String?,
  val startOdometer: Double?,
  val endOdometer: Double?,
  val status: String,
  val createdAt: Long,
  val completedAt: Long?,
  val closedEarly: Boolean,
  val closedBy: String?,
  val stagesJson: String
)

@Entity(tableName = "extra_trips")
data class ExtraTripEntity(
  @PrimaryKey val id: String,
  val driverId: String,
  val driverName: String,
  val bus: String,
  val startKm: Double?,
  val endKm: Double?,
  val distance: Double?,
  val note: String,
  val time: String,
  val createdAt: Long
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey val id: String,
  val driverId: String,
  val driverName: String,
  val bus: String,
  val type: String,
  val mode: String,
  val liters: Double?,
  val unitPrice: Double?,
  val amount: Double,
  val note: String,
  val receiptPath: String?,
  val createdAt: Long
)

@Entity(tableName = "buses")
data class BusEntity(
  @PrimaryKey val plateNumber: String,
  val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class QueueEntity(
  @PrimaryKey val id: String,
  val type: String,
  val title: String,
  val detail: String,
  val queuedAt: Long
)
