package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return cycles by code. */
interface LeaseReturnCycleStore {
    fun find(code: String): LeaseReturnCycle?
    fun save(item: LeaseReturnCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnCycleStore(
    var items: MutableMap<String, LeaseReturnCycle>,
    var order: MutableList<String>,
) : LeaseReturnCycleStore {
    override fun find(code: String): LeaseReturnCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnCycle) {
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

    override fun list(): MutableList<LeaseReturnCycle> {
        val out = mutableListOf<LeaseReturnCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnCycleStore(): LeaseReturnCycleStore {
    return MemoryLeaseReturnCycleStore(mutableMapOf<String, LeaseReturnCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnCycleStore(
    var inner: LeaseReturnCycleStore,
    var cache: TtlCache<LeaseReturnCycle>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnCycleStore {
    override fun find(code: String): LeaseReturnCycle? {
        val hit = (cache.get(code) as LeaseReturnCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnCycle> = inner.list()
}

fun newCachedLeaseReturnCycleStore(inner: LeaseReturnCycleStore, ttl: Int, clock: Clock): LeaseReturnCycleStore {
    return CachedLeaseReturnCycleStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return cycles. Empty fields match all. */
data class LeaseReturnCycleQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<LeaseReturnCycle>): MutableList<LeaseReturnCycle> {
        val matched = filter(items) { candidate: LeaseReturnCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnCycles(store: LeaseReturnCycleStore, query: LeaseReturnCycleQuery): MutableList<LeaseReturnCycle> {
    return query.apply(store.list())
}
