package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
  // Drivers
  @Query("SELECT * FROM drivers ORDER BY name ASC")
  fun getAllDrivers(): Flow<List<DriverEntity>>

  @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
  suspend fun getDriverById(id: String): DriverEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDriver(driver: DriverEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDrivers(drivers: List<DriverEntity>)

  @Update
  suspend fun updateDriver(driver: DriverEntity)

  @Query("DELETE FROM drivers WHERE id = :id")
  suspend fun deleteDriver(id: String)

  @Query("DELETE FROM drivers")
  suspend fun deleteAllDrivers()

  // Trips (Shifts)
  @Query("SELECT * FROM trips ORDER BY createdAt DESC")
  fun getAllTrips(): Flow<List<TripEntity>>

  @Query("SELECT * FROM trips WHERE driverId = :driverId ORDER BY createdAt DESC")
  fun getTripsForDriver(driverId: String): Flow<List<TripEntity>>

  @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
  suspend fun getTripById(id: String): TripEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrip(trip: TripEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrips(trips: List<TripEntity>)

  @Update
  suspend fun updateTrip(trip: TripEntity)

  @Query("DELETE FROM trips WHERE id = :id")
  suspend fun deleteTrip(id: String)

  @Query("DELETE FROM trips")
  suspend fun clearAllTrips()

  // Extra Trips
  @Query("SELECT * FROM extra_trips ORDER BY createdAt DESC")
  fun getAllExtraTrips(): Flow<List<ExtraTripEntity>>

  @Query("SELECT * FROM extra_trips WHERE driverId = :driverId ORDER BY createdAt DESC")
  fun getExtraTripsForDriver(driverId: String): Flow<List<ExtraTripEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExtraTrip(extraTrip: ExtraTripEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExtraTrips(extraTrips: List<ExtraTripEntity>)

  @Query("DELETE FROM extra_trips WHERE id = :id")
  suspend fun deleteExtraTrip(id: String)

  @Query("DELETE FROM extra_trips")
  suspend fun clearAllExtraTrips()

  // Expenses
  @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM expenses WHERE driverId = :driverId ORDER BY createdAt DESC")
  fun getExpensesForDriver(driverId: String): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenses(expenses: List<ExpenseEntity>)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpense(id: String)

  @Query("DELETE FROM expenses")
  suspend fun clearAllExpenses()

  // Buses
  @Query("SELECT * FROM buses ORDER BY plateNumber ASC")
  fun getAllBuses(): Flow<List<BusEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertBus(bus: BusEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertBuses(buses: List<BusEntity>)

  @Query("DELETE FROM buses WHERE plateNumber = :plateNumber")
  suspend fun deleteBus(plateNumber: String)

  @Query("DELETE FROM buses")
  suspend fun deleteAllBuses()

  // Sync Queue
  @Query("SELECT * FROM sync_queue ORDER BY queuedAt ASC")
  fun getAllQueue(): Flow<List<QueueEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQueueItem(item: QueueEntity)

  @Query("DELETE FROM sync_queue WHERE id = :id")
  suspend fun deleteQueueItem(id: String)

  @Query("DELETE FROM sync_queue")
  suspend fun clearAllQueue()

  // Counts for tester data summary
  @Query("SELECT COUNT(*) FROM trips")
  suspend fun getTripsCount(): Int

  @Query("SELECT COUNT(*) FROM extra_trips")
  suspend fun getExtraTripsCount(): Int

  @Query("SELECT COUNT(*) FROM expenses")
  suspend fun getExpensesCount(): Int

  @Query("SELECT COUNT(*) FROM sync_queue")
  suspend fun getQueueCount(): Int

  // Dedicated tester data purge transaction
  @Transaction
  suspend fun purgeAllTesterData(
    clearShifts: Boolean = true,
    clearExtras: Boolean = true,
    clearExpenses: Boolean = true,
    clearQueue: Boolean = true
  ) {
    if (clearShifts) clearAllTrips()
    if (clearExtras) clearAllExtraTrips()
    if (clearExpenses) clearAllExpenses()
    if (clearQueue) clearAllQueue()
  }
}
