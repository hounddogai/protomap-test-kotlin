package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy defect variances by code. */
interface LegacyDefectVarianceStore {
    fun find(code: String): LegacyDefectVariance?
    fun save(item: LegacyDefectVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDefectVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDefectVarianceStore(
    var items: MutableMap<String, LegacyDefectVariance>,
    var order: MutableList<String>,
) : LegacyDefectVarianceStore {
    override fun find(code: String): LegacyDefectVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDefectVariance) {
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

    override fun list(): MutableList<LegacyDefectVariance> {
        val out = mutableListOf<LegacyDefectVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDefectVarianceStore(): LegacyDefectVarianceStore {
    return MemoryLegacyDefectVarianceStore(
        mutableMapOf<String, LegacyDefectVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDefectVarianceStore(
    var inner: LegacyDefectVarianceStore,
    var cache: TtlCache<LegacyDefectVariance>,
    var hits: Int,
    var misses: Int,
) : LegacyDefectVarianceStore {
    override fun find(code: String): LegacyDefectVariance? {
        val hit = (cache.get(code) as LegacyDefectVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDefectVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDefectVariance> = inner.list()
}

fun newCachedLegacyDefectVarianceStore(inner: LegacyDefectVarianceStore, ttl: Int, clock: Clock): LegacyDefectVarianceStore {
    return CachedLegacyDefectVarianceStore(
        inner = inner,
        cache = newTtlCache<LegacyDefectVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy defect variances. Empty fields match all. */
data class LegacyDefectVarianceQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDefectVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LegacyDefectVariance>): MutableList<LegacyDefectVariance> {
        val matched = filter(items) { candidate: LegacyDefectVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDefectVariances(store: LegacyDefectVarianceStore, query: LegacyDefectVarianceQuery): MutableList<LegacyDefectVariance> {
    return query.apply(store.list())
}
