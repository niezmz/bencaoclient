package com.example.bencaoclient

import java.util.Date
import android.content.Context
import com.example.bencaoclient.db.AppDatabase
import com.example.bencaoclient.db.BencaoEntity
import com.example.bencaoclient.db.DbConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.ceil

enum class LibrarySortMode(val label: String) {
    CreatedAtAsc("时间正序"),
    CreatedAtDesc("时间倒序"),
    NameAsc("名称正序"),
    NameDesc("名称倒序");

    fun next(): LibrarySortMode = when (this) {
        CreatedAtAsc -> CreatedAtDesc
        CreatedAtDesc -> NameAsc
        NameAsc -> NameDesc
        NameDesc -> CreatedAtAsc
    }
}

object BencaoRepository {
    @Volatile
    private var initialized = false

    private lateinit var db: AppDatabase

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            db = AppDatabase.getInstance(context)
            initialized = true
        }
    }

    private fun requireInit() {
        check(initialized) { "BencaoRepository not initialized. Call BencaoRepository.init(context) first." }
    }

    suspend fun addBencao(bencao: Bencao) {
        requireInit()
        val entity = BencaoEntity(
            id = if (bencao.id != 0L) bencao.id else 0L,
            name = bencao.name,
            family = bencao.family,
            genus = bencao.genus,
            species = bencao.species,
            description = bencao.description,
            imagesJson = DbConverters.imagesToJson(bencao.images),
            rarity = bencao.rarity,
            isToxic = bencao.isToxic,
            isProtectedSpecies = bencao.isProtectedSpecies,
            isInvasiveSpecies = bencao.isInvasiveSpecies,
            plantingMethod = bencao.plantingMethod,
            isSuccess = bencao.isSuccess,
            createdAt = bencao.createdAt.time
        )
        withContext(Dispatchers.IO) {
            db.bencaoDao().insert(entity)
        }
    }

    suspend fun deleteBencaoById(id: Long) {
        requireInit()
        withContext(Dispatchers.IO) {
            db.bencaoDao().deleteById(id)
        }
    }

    suspend fun updateBencao(bencao: Bencao) {
        requireInit()
        check(bencao.id != 0L) { "updateBencao requires persisted id" }
        val entity = BencaoEntity(
            id = bencao.id,
            name = bencao.name,
            family = bencao.family,
            genus = bencao.genus,
            species = bencao.species,
            description = bencao.description,
            imagesJson = DbConverters.imagesToJson(bencao.images),
            rarity = bencao.rarity,
            isToxic = bencao.isToxic,
            isProtectedSpecies = bencao.isProtectedSpecies,
            isInvasiveSpecies = bencao.isInvasiveSpecies,
            plantingMethod = bencao.plantingMethod,
            isSuccess = bencao.isSuccess,
            createdAt = bencao.createdAt.time
        )
        withContext(Dispatchers.IO) {
            db.bencaoDao().update(entity)
        }
    }

    suspend fun getBencaoById(id: Long): Bencao? {
        requireInit()
        return withContext(Dispatchers.IO) {
            db.bencaoDao().getById(id)?.toModel()
        }
    }

    fun observeAllBencaos(): Flow<List<Bencao>> {
        requireInit()
        return db.bencaoDao().observeAll().map { list -> list.map { it.toModel() } }
    }

    fun observeLibraryPage(
        sortMode: LibrarySortMode,
        page: Int,
        pageSize: Int = 10
    ): Flow<List<Bencao>> {
        requireInit()
        val offset = page * pageSize
        val dao = db.bencaoDao()
        val flow = when (sortMode) {
            LibrarySortMode.CreatedAtAsc ->
                dao.observeLibraryPageCreatedAtAsc(limit = pageSize, offset = offset)
            LibrarySortMode.CreatedAtDesc ->
                dao.observeLibraryPageCreatedAtDesc(limit = pageSize, offset = offset)
            LibrarySortMode.NameAsc ->
                dao.observeLibraryPageNameAsc(limit = pageSize, offset = offset)
            LibrarySortMode.NameDesc ->
                dao.observeLibraryPageNameDesc(limit = pageSize, offset = offset)
        }
        return flow.map { list -> list.map { it.toModel() } }
    }

    fun observeAllBencaoIdsSorted(sortMode: LibrarySortMode): Flow<List<Long>> {
        requireInit()
        val dao = db.bencaoDao()
        return when (sortMode) {
            LibrarySortMode.CreatedAtAsc -> dao.observeAllIdsCreatedAtAsc()
            LibrarySortMode.CreatedAtDesc -> dao.observeAllIdsCreatedAtDesc()
            LibrarySortMode.NameAsc -> dao.observeAllIdsNameAsc()
            LibrarySortMode.NameDesc -> dao.observeAllIdsNameDesc()
        }
    }

    fun observeBencaoById(id: Long): Flow<Bencao?> {
        requireInit()
        return db.bencaoDao().observeById(id).map { entity -> entity?.toModel() }
    }

    suspend fun getAllBencaosSnapshot(): List<Bencao> {
        requireInit()
        return withContext(Dispatchers.IO) {
            db.bencaoDao().getAllSync().map { it.toModel() }
        }
    }

    suspend fun getGallerySpeciesSlice(offset: Int, limit: Int): List<Bencao> {
        requireInit()
        return withContext(Dispatchers.IO) {
            db.bencaoDao().getGallerySpeciesSlice(limit = limit, offset = offset).map { it.toModel() }
        }
    }

    suspend fun importBencaosMerge(items: List<Bencao>) {
        requireInit()
        if (items.isEmpty()) return
        withContext(Dispatchers.IO) {
            db.runInTransaction {
                val dao = db.bencaoDao()
                for (b in items) {
                    dao.insert(b.toNewEntity())
                }
            }
        }
    }

    suspend fun importBencaosReplaceAll(items: List<Bencao>) {
        requireInit()
        withContext(Dispatchers.IO) {
            db.runInTransaction {
                val dao = db.bencaoDao()
                dao.deleteAll()
                for (b in items) {
                    dao.insert(b.toNewEntity())
                }
            }
        }
    }

    private fun Bencao.toNewEntity(): BencaoEntity =
        BencaoEntity(
            id = 0L,
            name = name,
            family = family,
            genus = genus,
            species = species,
            description = description,
            imagesJson = DbConverters.imagesToJson(images),
            rarity = rarity,
            isToxic = isToxic,
            isProtectedSpecies = isProtectedSpecies,
            isInvasiveSpecies = isInvasiveSpecies,
            plantingMethod = plantingMethod,
            isSuccess = isSuccess,
            createdAt = createdAt.time
        )

    fun observeTotalPages(pageSize: Int = 10): Flow<Int> {
        requireInit()
        return db.bencaoDao().observeCount().map { count ->
            val pages = ceil(count / pageSize.toDouble()).toInt()
            maxOf(pages, 1)
        }
    }

    fun observePlantingMethodLibraryPage(
        sortMode: LibrarySortMode,
        page: Int,
        pageSize: Int = 10
    ): Flow<List<Bencao>> {
        requireInit()
        val offset = page * pageSize
        val dao = db.bencaoDao()
        val flow = when (sortMode) {
            LibrarySortMode.CreatedAtAsc ->
                dao.observePlantingMethodPageCreatedAtAsc(limit = pageSize, offset = offset)
            LibrarySortMode.CreatedAtDesc ->
                dao.observePlantingMethodPageCreatedAtDesc(limit = pageSize, offset = offset)
            LibrarySortMode.NameAsc ->
                dao.observePlantingMethodPageNameAsc(limit = pageSize, offset = offset)
            LibrarySortMode.NameDesc ->
                dao.observePlantingMethodPageNameDesc(limit = pageSize, offset = offset)
        }
        return flow.map { list -> list.map { it.toModel() } }
    }

    fun observePlantingMethodBencaoIdsSorted(sortMode: LibrarySortMode): Flow<List<Long>> {
        requireInit()
        val dao = db.bencaoDao()
        return when (sortMode) {
            LibrarySortMode.CreatedAtAsc -> dao.observePlantingMethodIdsCreatedAtAsc()
            LibrarySortMode.CreatedAtDesc -> dao.observePlantingMethodIdsCreatedAtDesc()
            LibrarySortMode.NameAsc -> dao.observePlantingMethodIdsNameAsc()
            LibrarySortMode.NameDesc -> dao.observePlantingMethodIdsNameDesc()
        }
    }

    fun observePlantingMethodTotalPages(pageSize: Int = 10): Flow<Int> {
        requireInit()
        return db.bencaoDao().observePlantingMethodCount().map { count ->
            val pages = ceil(count / pageSize.toDouble()).toInt()
            maxOf(pages, 1)
        }
    }

    fun observeFamilyPage(
        familyKey: String,
        sortMode: LibrarySortMode,
        page: Int,
        pageSize: Int = 10
    ): Flow<List<Bencao>> {
        requireInit()
        val offset = page * pageSize
        val dao = db.bencaoDao()
        val isBlankFamily = familyKey.trim() == "未填写"
        val flow = if (isBlankFamily) {
            when (sortMode) {
                LibrarySortMode.CreatedAtAsc ->
                    dao.observeFamilyBlankPageCreatedAtAsc(limit = pageSize, offset = offset)
                LibrarySortMode.CreatedAtDesc ->
                    dao.observeFamilyBlankPageCreatedAtDesc(limit = pageSize, offset = offset)
                LibrarySortMode.NameAsc ->
                    dao.observeFamilyBlankPageNameAsc(limit = pageSize, offset = offset)
                LibrarySortMode.NameDesc ->
                    dao.observeFamilyBlankPageNameDesc(limit = pageSize, offset = offset)
            }
        } else {
            when (sortMode) {
                LibrarySortMode.CreatedAtAsc ->
                    dao.observeFamilyPageCreatedAtAsc(family = familyKey, limit = pageSize, offset = offset)
                LibrarySortMode.CreatedAtDesc ->
                    dao.observeFamilyPageCreatedAtDesc(family = familyKey, limit = pageSize, offset = offset)
                LibrarySortMode.NameAsc ->
                    dao.observeFamilyPageNameAsc(family = familyKey, limit = pageSize, offset = offset)
                LibrarySortMode.NameDesc ->
                    dao.observeFamilyPageNameDesc(family = familyKey, limit = pageSize, offset = offset)
            }
        }
        return flow.map { list -> list.map { it.toModel() } }
    }

    fun observeFamilyBencaoIdsSorted(familyKey: String, sortMode: LibrarySortMode): Flow<List<Long>> {
        requireInit()
        val dao = db.bencaoDao()
        val isBlankFamily = familyKey.trim() == "未填写"
        return if (isBlankFamily) {
            when (sortMode) {
                LibrarySortMode.CreatedAtAsc -> dao.observeFamilyBlankIdsCreatedAtAsc()
                LibrarySortMode.CreatedAtDesc -> dao.observeFamilyBlankIdsCreatedAtDesc()
                LibrarySortMode.NameAsc -> dao.observeFamilyBlankIdsNameAsc()
                LibrarySortMode.NameDesc -> dao.observeFamilyBlankIdsNameDesc()
            }
        } else {
            when (sortMode) {
                LibrarySortMode.CreatedAtAsc -> dao.observeFamilyIdsCreatedAtAsc(familyKey)
                LibrarySortMode.CreatedAtDesc -> dao.observeFamilyIdsCreatedAtDesc(familyKey)
                LibrarySortMode.NameAsc -> dao.observeFamilyIdsNameAsc(familyKey)
                LibrarySortMode.NameDesc -> dao.observeFamilyIdsNameDesc(familyKey)
            }
        }
    }

    fun observeFamilyTotalPages(
        familyKey: String,
        pageSize: Int = 10
    ): Flow<Int> {
        requireInit()
        val dao = db.bencaoDao()
        val isBlankFamily = familyKey.trim() == "未填写"
        val countFlow = if (isBlankFamily) dao.observeFamilyBlankCount() else dao.observeFamilyCount(familyKey)
        return countFlow.map { count ->
            val pages = ceil(count / pageSize.toDouble()).toInt()
            maxOf(pages, 1)
        }
    }

    private fun BencaoEntity.toModel(): Bencao {
        return Bencao(
            id = id,
            name = name,
            family = family,
            genus = genus,
            species = species,
            description = description,
            images = DbConverters.jsonToImages(imagesJson),
            rarity = rarity.coerceIn(1, 5),
            isToxic = isToxic,
            isProtectedSpecies = isProtectedSpecies,
            isInvasiveSpecies = isInvasiveSpecies,
            plantingMethod = plantingMethod,
            isSuccess = isSuccess,
            createdAt = Date(createdAt)
        )
    }
}
