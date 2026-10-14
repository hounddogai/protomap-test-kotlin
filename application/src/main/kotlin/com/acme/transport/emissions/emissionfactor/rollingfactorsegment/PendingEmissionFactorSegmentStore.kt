package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending emission factor segments by code. */
interface PendingEmissionFactorSegmentStore {
    fun find(code: String): PendingEmissionFactorSegment?
    fun save(item: PendingEmissionFactorSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEmissionFactorSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEmissionFactorSegmentStore(
    var items: MutableMap<String, PendingEmissionFactorSegment>,
    var order: MutableList<String>,
) : PendingEmissionFactorSegmentStore {
    override fun find(code: String): PendingEmissionFactorSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEmissionFactorSegment) {
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

    override fun list(): MutableList<PendingEmissionFactorSegment> {
        val out = mutableListOf<PendingEmissionFactorSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEmissionFactorSegmentStore(): PendingEmissionFactorSegmentStore {
    return MemoryPendingEmissionFactorSegmentStore(
        mutableMapOf<String, PendingEmissionFactorSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEmissionFactorSegmentStore(
    var inner: PendingEmissionFactorSegmentStore,
    var cache: TtlCache<PendingEmissionFactorSegment>,
    var hits: Int,
    var misses: Int,
) : PendingEmissionFactorSegmentStore {
    override fun find(code: String): PendingEmissionFactorSegment? {
        val hit = (cache.get(code) as PendingEmissionFactorSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEmissionFactorSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEmissionFactorSegment> = inner.list()
}

fun newCachedPendingEmissionFactorSegmentStore(inner: PendingEmissionFactorSegmentStore, ttl: Int, clock: Clock): PendingEmissionFactorSegmentStore {
    return CachedPendingEmissionFactorSegmentStore(
        inner = inner,
        cache = newTtlCache<PendingEmissionFactorSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending emission factor segments. Empty fields match all. */
data class PendingEmissionFactorSegmentQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEmissionFactorSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingEmissionFactorSegment>): MutableList<PendingEmissionFactorSegment> {
        val matched = filter(items) { candidate: PendingEmissionFactorSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEmissionFactorSegments(store: PendingEmissionFactorSegmentStore, query: PendingEmissionFactorSegmentQuery): MutableList<PendingEmissionFactorSegment> {
    return query.apply(store.list())
}
