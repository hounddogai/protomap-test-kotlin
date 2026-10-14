package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return windows by code. */
interface LeaseReturnWindowStore {
    fun find(code: String): LeaseReturnWindow?
    fun save(item: LeaseReturnWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnWindowStore(
    var items: MutableMap<String, LeaseReturnWindow>,
    var order: MutableList<String>,
) : LeaseReturnWindowStore {
    override fun find(code: String): LeaseReturnWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnWindow) {
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

    override fun list(): MutableList<LeaseReturnWindow> {
        val out = mutableListOf<LeaseReturnWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnWindowStore(): LeaseReturnWindowStore {
    return MemoryLeaseReturnWindowStore(mutableMapOf<String, LeaseReturnWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnWindowStore(
    var inner: LeaseReturnWindowStore,
    var cache: TtlCache<LeaseReturnWindow>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnWindowStore {
    override fun find(code: String): LeaseReturnWindow? {
        val hit = (cache.get(code) as LeaseReturnWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnWindow> = inner.list()
}

fun newCachedLeaseReturnWindowStore(inner: LeaseReturnWindowStore, ttl: Int, clock: Clock): LeaseReturnWindowStore {
    return CachedLeaseReturnWindowStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return windows. Empty fields match all. */
data class LeaseReturnWindowQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnWindow>): MutableList<LeaseReturnWindow> {
        val matched = filter(items) { candidate: LeaseReturnWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnWindows(store: LeaseReturnWindowStore, query: LeaseReturnWindowQuery): MutableList<LeaseReturnWindow> {
    return query.apply(store.list())
}
