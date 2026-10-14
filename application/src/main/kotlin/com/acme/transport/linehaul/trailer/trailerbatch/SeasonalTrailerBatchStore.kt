package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal trailer batches by code. */
interface SeasonalTrailerBatchStore {
    fun find(code: String): SeasonalTrailerBatch?
    fun save(item: SeasonalTrailerBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerBatchStore(
    var items: MutableMap<String, SeasonalTrailerBatch>,
    var order: MutableList<String>,
) : SeasonalTrailerBatchStore {
    override fun find(code: String): SeasonalTrailerBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerBatch) {
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

    override fun list(): MutableList<SeasonalTrailerBatch> {
        val out = mutableListOf<SeasonalTrailerBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerBatchStore(): SeasonalTrailerBatchStore {
    return MemorySeasonalTrailerBatchStore(
        mutableMapOf<String, SeasonalTrailerBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerBatchStore(
    var inner: SeasonalTrailerBatchStore,
    var cache: TtlCache<SeasonalTrailerBatch>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerBatchStore {
    override fun find(code: String): SeasonalTrailerBatch? {
        val hit = (cache.get(code) as SeasonalTrailerBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerBatch> = inner.list()
}

fun newCachedSeasonalTrailerBatchStore(inner: SeasonalTrailerBatchStore, ttl: Int, clock: Clock): SeasonalTrailerBatchStore {
    return CachedSeasonalTrailerBatchStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer batches. Empty fields match all. */
data class SeasonalTrailerBatchQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerBatch>): MutableList<SeasonalTrailerBatch> {
        val matched = filter(items) { candidate: SeasonalTrailerBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerBatches(store: SeasonalTrailerBatchStore, query: SeasonalTrailerBatchQuery): MutableList<SeasonalTrailerBatch> {
    return query.apply(store.list())
}
