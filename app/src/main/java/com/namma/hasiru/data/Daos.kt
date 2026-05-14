package com.namma.hasiru.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantationDao {
    @Insert suspend fun insertPlantation(plantation: Plantation): Long
    @Update suspend fun updatePlantation(plantation: Plantation)
    @Delete suspend fun deletePlantation(plantation: Plantation)

    @Query("SELECT * FROM plantations ORDER BY plantedDate DESC")
    fun getAllPlantations(): Flow<List<Plantation>>

    @Query("SELECT * FROM plantations WHERE id = :id")
    fun getPlantationById(id: Long): Flow<Plantation?>

    @Query("SELECT * FROM plantations WHERE currentStatus != 'DIED' ORDER BY plantedDate DESC")
    fun getActivePlantations(): Flow<List<Plantation>>

    @Query("SELECT * FROM plantations WHERE nextReminderDate <= :currentDate AND reminderScheduled = 1")
    suspend fun getPlantationsNeedingReminder(currentDate: Long): List<Plantation>

    @Query("SELECT AVG(survivalScore) FROM plantations")
    fun getOverallSurvivalRate(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM plantations")
    fun getPlantationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM plantations WHERE currentStatus != 'DIED'")
    fun getActivePlantationsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM plantations WHERE nextReminderDate <= :currentDate AND reminderScheduled = 1")
    fun getReminderDueCount(currentDate: Long): Flow<Int>
}

@Dao
interface StatusUpdateDao {
    @Insert suspend fun insertStatusUpdate(update: StatusUpdate): Long
    @Delete suspend fun deleteStatusUpdate(update: StatusUpdate)

    @Query("SELECT * FROM status_updates WHERE plantationId = :plantationId ORDER BY updateDate DESC")
    fun getUpdatesForPlantation(plantationId: Long): Flow<List<StatusUpdate>>

    @Query("SELECT * FROM status_updates ORDER BY updateDate DESC LIMIT 10")
    fun getRecentUpdates(): Flow<List<StatusUpdate>>
}

@Dao
interface SpeciesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSpecies(species: Species)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(species: List<Species>)

    @Query("SELECT * FROM species ORDER BY commonName ASC")
    fun getAllSpecies(): Flow<List<Species>>

    @Query("SELECT * FROM species WHERE speciesName = :name")
    fun getSpeciesByName(name: String): Flow<Species?>

    @Query("SELECT * FROM species ORDER BY averageSurvivalRate DESC, totalPlanted DESC LIMIT 5")
    fun getTopPerformingSpecies(): Flow<List<Species>>

    @Query("UPDATE species SET totalPlanted = totalPlanted + 1 WHERE speciesName = :name")
    suspend fun incrementPlantedCount(name: String)

    @Query("UPDATE species SET totalSurvived = totalSurvived + 1 WHERE speciesName = :name")
    suspend fun incrementSurvivedCount(name: String)
}

@Dao
interface CommunityStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOrUpdateStats(stats: CommunityStats)

    @Query("SELECT * FROM community_stats ORDER BY survivalRate DESC")
    fun getAllRegionStats(): Flow<List<CommunityStats>>
}
