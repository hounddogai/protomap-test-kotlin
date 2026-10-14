package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending fuel burn thresholds by code. */
interface PendingFuelBurnThresholdStore {
    fun find(code: String): PendingFuelBurnThreshold?
    fun save(item: PendingFuelBurnThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFuelBurnThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFuelBurnThresholdStore(
    var items: MutableMap<String, PendingFuelBurnThreshold>,
    var order: MutableList<String>,
) : PendingFuelBurnThresholdStore {
    override fun find(code: String): PendingFuelBurnThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFuelBurnThreshold) {
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

    override fun list(): MutableList<PendingFuelBurnThreshold> {
        val out = mutableListOf<PendingFuelBurnThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFuelBurnThresholdStore(): PendingFuelBurnThresholdStore {
    return MemoryPendingFuelBurnThresholdStore(
        mutableMapOf<String, PendingFuelBurnThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFuelBurnThresholdStore(
    var inner: PendingFuelBurnThresholdStore,
    var cache: TtlCache<PendingFuelBurnThreshold>,
    var hits: Int,
    var misses: Int,
) : PendingFuelBurnThresholdStore {
    override fun find(code: String): PendingFuelBurnThreshold? {
        val hit = (cache.get(code) as PendingFuelBurnThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFuelBurnThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFuelBurnThreshold> = inner.list()
}

fun newCachedPendingFuelBurnThresholdStore(inner: PendingFuelBurnThresholdStore, ttl: Int, clock: Clock): PendingFuelBurnThresholdStore {
    return CachedPendingFuelBurnThresholdStore(
        inner = inner,
        cache = newTtlCache<PendingFuelBurnThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending fuel burn thresholds. Empty fields match all. */
data class PendingFuelBurnThresholdQuery(
    var region: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFuelBurnThreshold): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFuelBurnThreshold>): MutableList<PendingFuelBurnThreshold> {
        val matched = filter(items) { candidate: PendingFuelBurnThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFuelBurnThresholds(store: PendingFuelBurnThresholdStore, query: PendingFuelBurnThresholdQuery): MutableList<PendingFuelBurnThreshold> {
    return query.apply(store.list())
}
