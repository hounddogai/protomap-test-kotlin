package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization cycles by code. */
interface UtilizationCycleStore {
    fun find(code: String): UtilizationCycle?
    fun save(item: UtilizationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationCycleStore(
    var items: MutableMap<String, UtilizationCycle>,
    var order: MutableList<String>,
) : UtilizationCycleStore {
    override fun find(code: String): UtilizationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationCycle) {
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

    override fun list(): MutableList<UtilizationCycle> {
        val out = mutableListOf<UtilizationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationCycleStore(): UtilizationCycleStore {
    return MemoryUtilizationCycleStore(mutableMapOf<String, UtilizationCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationCycleStore(
    var inner: UtilizationCycleStore,
    var cache: TtlCache<UtilizationCycle>,
    var hits: Int,
    var misses: Int,
) : UtilizationCycleStore {
    override fun find(code: String): UtilizationCycle? {
        val hit = (cache.get(code) as UtilizationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationCycle> = inner.list()
}

fun newCachedUtilizationCycleStore(inner: UtilizationCycleStore, ttl: Int, clock: Clock): UtilizationCycleStore {
    return CachedUtilizationCycleStore(
        inner = inner,
        cache = newTtlCache<UtilizationCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization cycles. Empty fields match all. */
data class UtilizationCycleQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationCycle>): MutableList<UtilizationCycle> {
        val matched = filter(items) { candidate: UtilizationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationCycles(store: UtilizationCycleStore, query: UtilizationCycleQuery): MutableList<UtilizationCycle> {
    return query.apply(store.list())
}
