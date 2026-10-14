package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling lease return groups by code. */
interface RollingLeaseReturnGroupStore {
    fun find(code: String): RollingLeaseReturnGroup?
    fun save(item: RollingLeaseReturnGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLeaseReturnGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLeaseReturnGroupStore(
    var items: MutableMap<String, RollingLeaseReturnGroup>,
    var order: MutableList<String>,
) : RollingLeaseReturnGroupStore {
    override fun find(code: String): RollingLeaseReturnGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLeaseReturnGroup) {
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

    override fun list(): MutableList<RollingLeaseReturnGroup> {
        val out = mutableListOf<RollingLeaseReturnGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLeaseReturnGroupStore(): RollingLeaseReturnGroupStore {
    return MemoryRollingLeaseReturnGroupStore(
        mutableMapOf<String, RollingLeaseReturnGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLeaseReturnGroupStore(
    var inner: RollingLeaseReturnGroupStore,
    var cache: TtlCache<RollingLeaseReturnGroup>,
    var hits: Int,
    var misses: Int,
) : RollingLeaseReturnGroupStore {
    override fun find(code: String): RollingLeaseReturnGroup? {
        val hit = (cache.get(code) as RollingLeaseReturnGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLeaseReturnGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLeaseReturnGroup> = inner.list()
}

fun newCachedRollingLeaseReturnGroupStore(inner: RollingLeaseReturnGroupStore, ttl: Int, clock: Clock): RollingLeaseReturnGroupStore {
    return CachedRollingLeaseReturnGroupStore(
        inner = inner,
        cache = newTtlCache<RollingLeaseReturnGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lease return groups. Empty fields match all. */
data class RollingLeaseReturnGroupQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLeaseReturnGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingLeaseReturnGroup>): MutableList<RollingLeaseReturnGroup> {
        val matched = filter(items) { candidate: RollingLeaseReturnGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLeaseReturnGroups(store: RollingLeaseReturnGroupStore, query: RollingLeaseReturnGroupQuery): MutableList<RollingLeaseReturnGroup> {
    return query.apply(store.list())
}
