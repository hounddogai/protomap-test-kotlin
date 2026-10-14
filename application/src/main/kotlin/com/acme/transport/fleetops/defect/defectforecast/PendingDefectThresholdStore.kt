package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending defect thresholds by code. */
interface PendingDefectThresholdStore {
    fun find(code: String): PendingDefectThreshold?
    fun save(item: PendingDefectThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDefectThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDefectThresholdStore(
    var items: MutableMap<String, PendingDefectThreshold>,
    var order: MutableList<String>,
) : PendingDefectThresholdStore {
    override fun find(code: String): PendingDefectThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDefectThreshold) {
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

    override fun list(): MutableList<PendingDefectThreshold> {
        val out = mutableListOf<PendingDefectThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDefectThresholdStore(): PendingDefectThresholdStore {
    return MemoryPendingDefectThresholdStore(
        mutableMapOf<String, PendingDefectThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDefectThresholdStore(
    var inner: PendingDefectThresholdStore,
    var cache: TtlCache<PendingDefectThreshold>,
    var hits: Int,
    var misses: Int,
) : PendingDefectThresholdStore {
    override fun find(code: String): PendingDefectThreshold? {
        val hit = (cache.get(code) as PendingDefectThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDefectThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDefectThreshold> = inner.list()
}

fun newCachedPendingDefectThresholdStore(inner: PendingDefectThresholdStore, ttl: Int, clock: Clock): PendingDefectThresholdStore {
    return CachedPendingDefectThresholdStore(
        inner = inner,
        cache = newTtlCache<PendingDefectThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending defect thresholds. Empty fields match all. */
data class PendingDefectThresholdQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDefectThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDefectThreshold>): MutableList<PendingDefectThreshold> {
        val matched = filter(items) { candidate: PendingDefectThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDefectThresholds(store: PendingDefectThresholdStore, query: PendingDefectThresholdQuery): MutableList<PendingDefectThreshold> {
    return query.apply(store.list())
}
