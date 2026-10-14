package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal declaration batches by code. */
interface SeasonalDeclarationBatchStore {
    fun find(code: String): SeasonalDeclarationBatch?
    fun save(item: SeasonalDeclarationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDeclarationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDeclarationBatchStore(
    var items: MutableMap<String, SeasonalDeclarationBatch>,
    var order: MutableList<String>,
) : SeasonalDeclarationBatchStore {
    override fun find(code: String): SeasonalDeclarationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDeclarationBatch) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<SeasonalDeclarationBatch> {
        val out = mutableListOf<SeasonalDeclarationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDeclarationBatchStore(): SeasonalDeclarationBatchStore {
    return MemorySeasonalDeclarationBatchStore(
        mutableMapOf<String, SeasonalDeclarationBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDeclarationBatchStore(
    var inner: SeasonalDeclarationBatchStore,
    var cache: TtlCache<SeasonalDeclarationBatch>,
    var hits: Int,
    var misses: Int,
) : SeasonalDeclarationBatchStore {
    override fun find(code: String): SeasonalDeclarationBatch? {
        val hit = (cache.get(code) as SeasonalDeclarationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDeclarationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDeclarationBatch> = inner.list()
}

fun newCachedSeasonalDeclarationBatchStore(inner: SeasonalDeclarationBatchStore, ttl: Int, clock: Clock): SeasonalDeclarationBatchStore {
    return CachedSeasonalDeclarationBatchStore(
        inner = inner,
        cache = newTtlCache<SeasonalDeclarationBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal declaration batches. Empty fields match all. */
data class SeasonalDeclarationBatchQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDeclarationBatch): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDeclarationBatch>): MutableList<SeasonalDeclarationBatch> {
        val matched = filter(items) { candidate: SeasonalDeclarationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDeclarationBatches(store: SeasonalDeclarationBatchStore, query: SeasonalDeclarationBatchQuery): MutableList<SeasonalDeclarationBatch> {
    return query.apply(store.list())
}
