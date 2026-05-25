package com.example.bencaoclient.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BencaoEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bencaoDao(): BencaoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bencao ADD COLUMN family TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE bencao ADD COLUMN genus TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE bencao ADD COLUMN species TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bencao ADD COLUMN rarity INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE bencao ADD COLUMN isToxic INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE bencao ADD COLUMN isProtectedSpecies INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE bencao ADD COLUMN isInvasiveSpecies INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bencao ADD COLUMN planting_method TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE bencao ADD COLUMN isSuccess INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bencaoclient.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

