package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional utilization cycles by code. */
interface RegionalUtilizationCycleStore {
    fun find(code: String): RegionalUtilizationCycle?
    fun save(item: RegionalUtilizationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalUtilizationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalUtilizationCycleStore(
    var items: MutableMap<String, RegionalUtilizationCycle>,
    var order: MutableList<String>,
) : RegionalUtilizationCycleStore {
    override fun find(code: String): RegionalUtilizationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalUtilizationCycle) {
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

    override fun list(): MutableList<RegionalUtilizationCycle> {
        val out = mutableListOf<RegionalUtilizationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalUtilizationCycleStore(): RegionalUtilizationCycleStore {
    return MemoryRegionalUtilizationCycleStore(
        mutableMapOf<String, RegionalUtilizationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalUtilizationCycleStore(
    var inner: RegionalUtilizationCycleStore,
    var cache: TtlCache<RegionalUtilizationCycle>,
    var hits: Int,
    var misses: Int,
) : RegionalUtilizationCycleStore {
    override fun find(code: String): RegionalUtilizationCycle? {
        val hit = (cache.get(code) as RegionalUtilizationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalUtilizationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalUtilizationCycle> = inner.list()
}

fun newCachedRegionalUtilizationCycleStore(inner: RegionalUtilizationCycleStore, ttl: Int, clock: Clock): RegionalUtilizationCycleStore {
    return CachedRegionalUtilizationCycleStore(
        inner = inner,
        cache = newTtlCache<RegionalUtilizationCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional utilization cycles. Empty fields match all. */
data class RegionalUtilizationCycleQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalUtilizationCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalUtilizationCycle>): MutableList<RegionalUtilizationCycle> {
        val matched = filter(items) { candidate: RegionalUtilizationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalUtilizationCycles(store: RegionalUtilizationCycleStore, query: RegionalUtilizationCycleQuery): MutableList<RegionalUtilizationCycle> {
    return query.apply(store.list())
}
