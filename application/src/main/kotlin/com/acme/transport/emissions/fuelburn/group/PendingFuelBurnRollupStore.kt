package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending fuel burn rollups by code. */
interface PendingFuelBurnRollupStore {
    fun find(code: String): PendingFuelBurnRollup?
    fun save(item: PendingFuelBurnRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFuelBurnRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFuelBurnRollupStore(
    var items: MutableMap<String, PendingFuelBurnRollup>,
    var order: MutableList<String>,
) : PendingFuelBurnRollupStore {
    override fun find(code: String): PendingFuelBurnRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFuelBurnRollup) {
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

    override fun list(): MutableList<PendingFuelBurnRollup> {
        val out = mutableListOf<PendingFuelBurnRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFuelBurnRollupStore(): PendingFuelBurnRollupStore {
    return MemoryPendingFuelBurnRollupStore(
        mutableMapOf<String, PendingFuelBurnRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFuelBurnRollupStore(
    var inner: PendingFuelBurnRollupStore,
    var cache: TtlCache<PendingFuelBurnRollup>,
    var hits: Int,
    var misses: Int,
) : PendingFuelBurnRollupStore {
    override fun find(code: String): PendingFuelBurnRollup? {
        val hit = (cache.get(code) as PendingFuelBurnRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFuelBurnRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFuelBurnRollup> = inner.list()
}

fun newCachedPendingFuelBurnRollupStore(inner: PendingFuelBurnRollupStore, ttl: Int, clock: Clock): PendingFuelBurnRollupStore {
    return CachedPendingFuelBurnRollupStore(
        inner = inner,
        cache = newTtlCache<PendingFuelBurnRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending fuel burn rollups. Empty fields match all. */
data class PendingFuelBurnRollupQuery(
    var country: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFuelBurnRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFuelBurnRollup>): MutableList<PendingFuelBurnRollup> {
        val matched = filter(items) { candidate: PendingFuelBurnRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFuelBurnRollups(store: PendingFuelBurnRollupStore, query: PendingFuelBurnRollupQuery): MutableList<PendingFuelBurnRollup> {
    return query.apply(store.list())
}
