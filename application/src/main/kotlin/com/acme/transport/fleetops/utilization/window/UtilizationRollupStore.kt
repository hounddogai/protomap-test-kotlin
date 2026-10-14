package com.acme.transport.fleetops.utilization.window

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization rollups by code. */
interface UtilizationRollupStore {
    fun find(code: String): UtilizationRollup?
    fun save(item: UtilizationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationRollupStore(
    var items: MutableMap<String, UtilizationRollup>,
    var order: MutableList<String>,
) : UtilizationRollupStore {
    override fun find(code: String): UtilizationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationRollup) {
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

    override fun list(): MutableList<UtilizationRollup> {
        val out = mutableListOf<UtilizationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationRollupStore(): UtilizationRollupStore {
    return MemoryUtilizationRollupStore(mutableMapOf<String, UtilizationRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationRollupStore(
    var inner: UtilizationRollupStore,
    var cache: TtlCache<UtilizationRollup>,
    var hits: Int,
    var misses: Int,
) : UtilizationRollupStore {
    override fun find(code: String): UtilizationRollup? {
        val hit = (cache.get(code) as UtilizationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationRollup> = inner.list()
}

fun newCachedUtilizationRollupStore(inner: UtilizationRollupStore, ttl: Int, clock: Clock): UtilizationRollupStore {
    return CachedUtilizationRollupStore(
        inner = inner,
        cache = newTtlCache<UtilizationRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization rollups. Empty fields match all. */
data class UtilizationRollupQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationRollup>): MutableList<UtilizationRollup> {
        val matched = filter(items) { candidate: UtilizationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationRollups(store: UtilizationRollupStore, query: UtilizationRollupQuery): MutableList<UtilizationRollup> {
    return query.apply(store.list())
}
