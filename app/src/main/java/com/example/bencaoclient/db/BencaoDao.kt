package com.example.bencaoclient.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BencaoDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entity: BencaoEntity): Long

    @Update
    fun update(entity: BencaoEntity): Int

    @Query("SELECT * FROM bencao ORDER BY createdAt ASC, id ASC LIMIT :limit OFFSET :offset")
    fun observeLibraryPageCreatedAtAsc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query("SELECT id FROM bencao ORDER BY createdAt ASC, id ASC")
    fun observeAllIdsCreatedAtAsc(): Flow<List<Long>>

    @Query("SELECT * FROM bencao ORDER BY createdAt DESC, id DESC LIMIT :limit OFFSET :offset")
    fun observeLibraryPageCreatedAtDesc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query("SELECT id FROM bencao ORDER BY createdAt DESC, id DESC")
    fun observeAllIdsCreatedAtDesc(): Flow<List<Long>>

    @Query("SELECT * FROM bencao ORDER BY name ASC, id ASC LIMIT :limit OFFSET :offset")
    fun observeLibraryPageNameAsc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query("SELECT id FROM bencao ORDER BY name ASC, id ASC")
    fun observeAllIdsNameAsc(): Flow<List<Long>>

    @Query("SELECT * FROM bencao ORDER BY name DESC, id DESC LIMIT :limit OFFSET :offset")
    fun observeLibraryPageNameDesc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query("SELECT id FROM bencao ORDER BY name DESC, id DESC")
    fun observeAllIdsNameDesc(): Flow<List<Long>>

    @Query("SELECT * FROM bencao ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BencaoEntity>>

    @Query("SELECT * FROM bencao WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<BencaoEntity?>

    @Query("SELECT COUNT(*) FROM bencao")
    fun observeCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY createdAt ASC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observePlantingMethodPageCreatedAtAsc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observePlantingMethodPageCreatedAtDesc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY name ASC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observePlantingMethodPageNameAsc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY name DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observePlantingMethodPageNameDesc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY createdAt ASC, id ASC
        """
    )
    fun observePlantingMethodIdsCreatedAtAsc(): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY createdAt DESC, id DESC
        """
    )
    fun observePlantingMethodIdsCreatedAtDesc(): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY name ASC, id ASC
        """
    )
    fun observePlantingMethodIdsNameAsc(): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0
        ORDER BY name DESC, id DESC
        """
    )
    fun observePlantingMethodIdsNameDesc(): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM bencao WHERE planting_method IS NOT NULL AND length(trim(planting_method)) > 0")
    fun observePlantingMethodCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY createdAt ASC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyPageCreatedAtAsc(family: String, limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyPageCreatedAtDesc(family: String, limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY name ASC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyPageNameAsc(family: String, limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY name DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyPageNameDesc(family: String, limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query("SELECT COUNT(*) FROM bencao WHERE trim(family) = trim(:family)")
    fun observeFamilyCount(family: String): Flow<Int>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY createdAt ASC, id ASC
        """
    )
    fun observeFamilyIdsCreatedAtAsc(family: String): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY createdAt DESC, id DESC
        """
    )
    fun observeFamilyIdsCreatedAtDesc(family: String): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY name ASC, id ASC
        """
    )
    fun observeFamilyIdsNameAsc(family: String): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = trim(:family)
        ORDER BY name DESC, id DESC
        """
    )
    fun observeFamilyIdsNameDesc(family: String): Flow<List<Long>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY createdAt ASC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyBlankPageCreatedAtAsc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyBlankPageCreatedAtDesc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY name ASC, id ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyBlankPageNameAsc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query(
        """
        SELECT * FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY name DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeFamilyBlankPageNameDesc(limit: Int, offset: Int): Flow<List<BencaoEntity>>

    @Query("SELECT COUNT(*) FROM bencao WHERE trim(family) = '' OR family IS NULL")
    fun observeFamilyBlankCount(): Flow<Int>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY createdAt ASC, id ASC
        """
    )
    fun observeFamilyBlankIdsCreatedAtAsc(): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY createdAt DESC, id DESC
        """
    )
    fun observeFamilyBlankIdsCreatedAtDesc(): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY name ASC, id ASC
        """
    )
    fun observeFamilyBlankIdsNameAsc(): Flow<List<Long>>

    @Query(
        """
        SELECT id FROM bencao
        WHERE trim(family) = '' OR family IS NULL
        ORDER BY name DESC, id DESC
        """
    )
    fun observeFamilyBlankIdsNameDesc(): Flow<List<Long>>

    @Query("SELECT * FROM bencao WHERE id = :id LIMIT 1")
    fun getById(id: Long): BencaoEntity?

    @Query("SELECT * FROM bencao ORDER BY createdAt DESC, id DESC LIMIT :limit OFFSET :offset")
    fun getGallerySpeciesSlice(limit: Int, offset: Int): List<BencaoEntity>

    @Query("SELECT * FROM bencao")
    fun getAllSync(): List<BencaoEntity>

    @Query("DELETE FROM bencao WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("DELETE FROM bencao")
    fun deleteAll(): Int
}
