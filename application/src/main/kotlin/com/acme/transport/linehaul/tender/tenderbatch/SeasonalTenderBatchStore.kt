package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal tender batches by code. */
interface SeasonalTenderBatchStore {
    fun find(code: String): SeasonalTenderBatch?
    fun save(item: SeasonalTenderBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTenderBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTenderBatchStore(
    var items: MutableMap<String, SeasonalTenderBatch>,
    var order: MutableList<String>,
) : SeasonalTenderBatchStore {
    override fun find(code: String): SeasonalTenderBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTenderBatch) {
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

    override fun list(): MutableList<SeasonalTenderBatch> {
        val out = mutableListOf<SeasonalTenderBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTenderBatchStore(): SeasonalTenderBatchStore {
    return MemorySeasonalTenderBatchStore(mutableMapOf<String, SeasonalTenderBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTenderBatchStore(
    var inner: SeasonalTenderBatchStore,
    var cache: TtlCache<SeasonalTenderBatch>,
    var hits: Int,
    var misses: Int,
) : SeasonalTenderBatchStore {
    override fun find(code: String): SeasonalTenderBatch? {
        val hit = (cache.get(code) as SeasonalTenderBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTenderBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTenderBatch> = inner.list()
}

fun newCachedSeasonalTenderBatchStore(inner: SeasonalTenderBatchStore, ttl: Int, clock: Clock): SeasonalTenderBatchStore {
    return CachedSeasonalTenderBatchStore(
        inner = inner,
        cache = newTtlCache<SeasonalTenderBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tender batches. Empty fields match all. */
data class SeasonalTenderBatchQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTenderBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTenderBatch>): MutableList<SeasonalTenderBatch> {
        val matched = filter(items) { candidate: SeasonalTenderBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTenderBatches(store: SeasonalTenderBatchStore, query: SeasonalTenderBatchQuery): MutableList<SeasonalTenderBatch> {
    return query.apply(store.list())
}
