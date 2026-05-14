package com.namma.hasiru.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HasiruConverters {
    @TypeConverter fun plantTypeToString(value: PlantType): String = value.name
    @TypeConverter fun plantTypeFromString(value: String): PlantType = PlantType.valueOf(value)
    @TypeConverter fun statusToString(value: PlantationStatus): String = value.name
    @TypeConverter fun statusFromString(value: String): PlantationStatus = PlantationStatus.valueOf(value)
}

@Database(
    entities = [Plantation::class, StatusUpdate::class, Species::class, CommunityStats::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(HasiruConverters::class)
abstract class NammaHasiruDatabase : RoomDatabase() {
    abstract fun plantationDao(): PlantationDao
    abstract fun statusUpdateDao(): StatusUpdateDao
    abstract fun speciesDao(): SpeciesDao
    abstract fun communityStatsDao(): CommunityStatsDao

    companion object {
        @Volatile private var INSTANCE: NammaHasiruDatabase? = null

        fun getDatabase(context: Context): NammaHasiruDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NammaHasiruDatabase::class.java,
                    "namma_hasiru_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).speciesDao().insertAll(initialSpecies())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }

        private fun initialSpecies() = listOf(
            Species("neem", "Neem", "Azadirachta indica", "TREE", "Loamy", "LOW", "FULL_SUN", 85f, 42, 36, description = "Hardy native tree with medicinal value and strong drought tolerance."),
            Species("peepal", "Peepal", "Ficus religiosa", "TREE", "Well-drained", "MEDIUM", "FULL_SUN", 78f, 28, 22, description = "Long-living shade tree valued for broad canopy and biodiversity support."),
            Species("banyan", "Banyan", "Ficus benghalensis", "TREE", "Rich soil", "MEDIUM", "FULL_SUN", 72f, 19, 14, description = "Large canopy tree with aerial roots, best for open community spaces."),
            Species("mango", "Mango", "Mangifera indica", "TREE", "Well-drained", "MEDIUM", "FULL_SUN", 68f, 31, 21, description = "Fruit-bearing tree that needs sunlight and early care."),
            Species("jamun", "Jamun", "Syzygium cumini", "TREE", "Loamy", "MEDIUM", "FULL_SUN", 74f, 24, 18, description = "Native fruiting tree that performs well in warm Indian climates."),
            Species("honnavara", "Indian Beech", "Pongamia pinnata", "TREE", "Sandy loam", "LOW", "FULL_SUN", 81f, 16, 13, description = "Nitrogen-fixing native tree useful for roadside and lake-side restoration."),
            Species("amla", "Amla", "Phyllanthus emblica", "TREE", "Well-drained", "LOW", "FULL_SUN", 76f, 22, 17, description = "Resilient fruiting tree with good survival in dry conditions.")
        )
    }
}
