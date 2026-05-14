package com.namma.hasiru.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit

class PlantationRepository(private val db: NammaHasiruDatabase) {
    val plantations: Flow<List<Plantation>> = db.plantationDao().getAllPlantations()
    val species: Flow<List<Species>> = db.speciesDao().getAllSpecies()
    val topSpecies: Flow<List<Species>> = db.speciesDao().getTopPerformingSpecies()

    val userStats: Flow<UserStats> = combine(
        db.plantationDao().getPlantationsCount(),
        db.plantationDao().getActivePlantationsCount(),
        db.plantationDao().getOverallSurvivalRate(),
        db.plantationDao().getReminderDueCount(System.currentTimeMillis())
    ) { total, active, survival, due ->
        UserStats(total, active, survival ?: 0f, due)
    }

    fun plantation(id: Long) = db.plantationDao().getPlantationById(id)
    fun updates(id: Long) = db.statusUpdateDao().getUpdatesForPlantation(id)

    suspend fun addPlantation(plantation: Plantation): Long {
        val id = db.plantationDao().insertPlantation(plantation)
        db.speciesDao().incrementPlantedCount(plantation.speciesName)
        return id
    }

    suspend fun deletePlantation(plantation: Plantation) = db.plantationDao().deletePlantation(plantation)

    suspend fun addStatusUpdate(plantation: Plantation, update: StatusUpdate) {
        db.statusUpdateDao().insertStatusUpdate(update)
        db.plantationDao().updatePlantation(
            plantation.copy(
                currentStatus = update.status,
                lastCheckedDate = update.updateDate,
                survivalScore = update.survivalScore,
                nextReminderDate = update.updateDate + TimeUnit.DAYS.toMillis(90)
            )
        )
        if (update.status != PlantationStatus.DIED) db.speciesDao().incrementSurvivedCount(plantation.speciesName)
    }

    suspend fun deleteStatusUpdate(update: StatusUpdate) = db.statusUpdateDao().deleteStatusUpdate(update)
    suspend fun updatePlantation(plantation: Plantation) = db.plantationDao().updatePlantation(plantation)
}
