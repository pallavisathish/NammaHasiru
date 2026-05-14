package com.namma.hasiru.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PlantType { SEED_BALL, SAPLING }
enum class PlantationStatus { PLANTED, SPROUTED, GROWING, HEALTHY, DIED }

@Entity(tableName = "plantations")
data class Plantation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val speciesName: String,
    val commonName: String,
    val plantedDate: Long,
    val latitude: Double,
    val longitude: Double,
    val locationAddress: String,
    val initialPhotoUri: String,
    val plantType: PlantType,
    val currentStatus: PlantationStatus,
    val lastCheckedDate: Long? = null,
    val survivalScore: Int = 0,
    val soilType: String? = null,
    val notes: String? = null,
    val reminderScheduled: Boolean = true,
    val nextReminderDate: Long? = null
)

@Entity(
    tableName = "status_updates",
    foreignKeys = [
        ForeignKey(
            entity = Plantation::class,
            parentColumns = ["id"],
            childColumns = ["plantationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantationId")]
)
data class StatusUpdate(
    @PrimaryKey(autoGenerate = true) val updateId: Long = 0,
    val plantationId: Long,
    val updateDate: Long,
    val status: PlantationStatus,
    val photoUri: String? = null,
    val heightCm: Float? = null,
    val healthNotes: String? = null,
    val survivalScore: Int
)

@Entity(tableName = "species")
data class Species(
    @PrimaryKey val speciesName: String,
    val commonName: String,
    val scientificName: String,
    val category: String,
    val idealSoilType: String,
    val waterRequirement: String,
    val sunlightRequirement: String,
    val averageSurvivalRate: Float = 0f,
    val totalPlanted: Int = 0,
    val totalSurvived: Int = 0,
    val imageUrl: String? = null,
    val description: String? = null
)

@Entity(tableName = "community_stats")
data class CommunityStats(
    @PrimaryKey val region: String,
    val totalPlantations: Int = 0,
    val survivalRate: Float = 0f,
    val mostSuccessfulSpecies: String? = null,
    val lastUpdated: Long
)

data class UserStats(
    val totalPlanted: Int = 0,
    val activeCount: Int = 0,
    val survivalRate: Float = 0f,
    val remindersDue: Int = 0
)
