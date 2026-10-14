package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return groups by code. */
interface LeaseReturnGroupStore {
    fun find(code: String): LeaseReturnGroup?
    fun save(item: LeaseReturnGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnGroupStore(
    var items: MutableMap<String, LeaseReturnGroup>,
    var order: MutableList<String>,
) : LeaseReturnGroupStore {
    override fun find(code: String): LeaseReturnGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnGroup) {
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

    override fun list(): MutableList<LeaseReturnGroup> {
        val out = mutableListOf<LeaseReturnGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnGroupStore(): LeaseReturnGroupStore {
    return MemoryLeaseReturnGroupStore(mutableMapOf<String, LeaseReturnGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnGroupStore(
    var inner: LeaseReturnGroupStore,
    var cache: TtlCache<LeaseReturnGroup>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnGroupStore {
    override fun find(code: String): LeaseReturnGroup? {
        val hit = (cache.get(code) as LeaseReturnGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnGroup> = inner.list()
}

fun newCachedLeaseReturnGroupStore(inner: LeaseReturnGroupStore, ttl: Int, clock: Clock): LeaseReturnGroupStore {
    return CachedLeaseReturnGroupStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return groups. Empty fields match all. */
data class LeaseReturnGroupQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnGroup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnGroup>): MutableList<LeaseReturnGroup> {
        val matched = filter(items) { candidate: LeaseReturnGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnGroups(store: LeaseReturnGroupStore, query: LeaseReturnGroupQuery): MutableList<LeaseReturnGroup> {
    return query.apply(store.list())
}
