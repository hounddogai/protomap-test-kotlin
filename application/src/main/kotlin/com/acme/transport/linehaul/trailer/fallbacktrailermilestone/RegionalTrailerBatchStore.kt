package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional trailer batches by code. */
interface RegionalTrailerBatchStore {
    fun find(code: String): RegionalTrailerBatch?
    fun save(item: RegionalTrailerBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerBatchStore(
    var items: MutableMap<String, RegionalTrailerBatch>,
    var order: MutableList<String>,
) : RegionalTrailerBatchStore {
    override fun find(code: String): RegionalTrailerBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerBatch) {
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

    override fun list(): MutableList<RegionalTrailerBatch> {
        val out = mutableListOf<RegionalTrailerBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerBatchStore(): RegionalTrailerBatchStore {
    return MemoryRegionalTrailerBatchStore(
        mutableMapOf<String, RegionalTrailerBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerBatchStore(
    var inner: RegionalTrailerBatchStore,
    var cache: TtlCache<RegionalTrailerBatch>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerBatchStore {
    override fun find(code: String): RegionalTrailerBatch? {
        val hit = (cache.get(code) as RegionalTrailerBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerBatch> = inner.list()
}

fun newCachedRegionalTrailerBatchStore(inner: RegionalTrailerBatchStore, ttl: Int, clock: Clock): RegionalTrailerBatchStore {
    return CachedRegionalTrailerBatchStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer batches. Empty fields match all. */
data class RegionalTrailerBatchQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerBatch>): MutableList<RegionalTrailerBatch> {
        val matched = filter(items) { candidate: RegionalTrailerBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerBatches(store: RegionalTrailerBatchStore, query: RegionalTrailerBatchQuery): MutableList<RegionalTrailerBatch> {
    return query.apply(store.list())
}
