package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return rollups by code. */
interface LeaseReturnRollupStore {
    fun find(code: String): LeaseReturnRollup?
    fun save(item: LeaseReturnRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnRollupStore(
    var items: MutableMap<String, LeaseReturnRollup>,
    var order: MutableList<String>,
) : LeaseReturnRollupStore {
    override fun find(code: String): LeaseReturnRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnRollup) {
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

    override fun list(): MutableList<LeaseReturnRollup> {
        val out = mutableListOf<LeaseReturnRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnRollupStore(): LeaseReturnRollupStore {
    return MemoryLeaseReturnRollupStore(mutableMapOf<String, LeaseReturnRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnRollupStore(
    var inner: LeaseReturnRollupStore,
    var cache: TtlCache<LeaseReturnRollup>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnRollupStore {
    override fun find(code: String): LeaseReturnRollup? {
        val hit = (cache.get(code) as LeaseReturnRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnRollup> = inner.list()
}

fun newCachedLeaseReturnRollupStore(inner: LeaseReturnRollupStore, ttl: Int, clock: Clock): LeaseReturnRollupStore {
    return CachedLeaseReturnRollupStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return rollups. Empty fields match all. */
data class LeaseReturnRollupQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnRollup>): MutableList<LeaseReturnRollup> {
        val matched = filter(items) { candidate: LeaseReturnRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnRollups(store: LeaseReturnRollupStore, query: LeaseReturnRollupQuery): MutableList<LeaseReturnRollup> {
    return query.apply(store.list())
}
