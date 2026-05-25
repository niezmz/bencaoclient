package com.example.bencaoclient.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bencao")
data class BencaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val family: String,
    val genus: String,
    val species: String,
    val description: String,
    val imagesJson: String,
    val rarity: Int,
    val isToxic: Boolean,
    val isProtectedSpecies: Boolean,
    val isInvasiveSpecies: Boolean,
    @ColumnInfo(name = "planting_method") val plantingMethod: String = "",
    val isSuccess: Boolean = false,
    /** epoch millis */
    val createdAt: Long
)

