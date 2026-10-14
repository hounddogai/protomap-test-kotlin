package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return cycles by code. */
interface RegionalLeaseReturnCycleStore {
    fun find(code: String): RegionalLeaseReturnCycle?
    fun save(item: RegionalLeaseReturnCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnCycleStore(
    var items: MutableMap<String, RegionalLeaseReturnCycle>,
    var order: MutableList<String>,
) : RegionalLeaseReturnCycleStore {
    override fun find(code: String): RegionalLeaseReturnCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnCycle) {
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

    override fun list(): MutableList<RegionalLeaseReturnCycle> {
        val out = mutableListOf<RegionalLeaseReturnCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnCycleStore(): RegionalLeaseReturnCycleStore {
    return MemoryRegionalLeaseReturnCycleStore(
        mutableMapOf<String, RegionalLeaseReturnCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnCycleStore(
    var inner: RegionalLeaseReturnCycleStore,
    var cache: TtlCache<RegionalLeaseReturnCycle>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnCycleStore {
    override fun find(code: String): RegionalLeaseReturnCycle? {
        val hit = (cache.get(code) as RegionalLeaseReturnCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnCycle> = inner.list()
}

fun newCachedRegionalLeaseReturnCycleStore(inner: RegionalLeaseReturnCycleStore, ttl: Int, clock: Clock): RegionalLeaseReturnCycleStore {
    return CachedRegionalLeaseReturnCycleStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return cycles. Empty fields match all. */
data class RegionalLeaseReturnCycleQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<RegionalLeaseReturnCycle>): MutableList<RegionalLeaseReturnCycle> {
        val matched = filter(items) { candidate: RegionalLeaseReturnCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnCycles(store: RegionalLeaseReturnCycleStore, query: RegionalLeaseReturnCycleQuery): MutableList<RegionalLeaseReturnCycle> {
    return query.apply(store.list())
}
