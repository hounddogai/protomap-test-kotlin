package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return windows by code. */
interface RegionalLeaseReturnWindowStore {
    fun find(code: String): RegionalLeaseReturnWindow?
    fun save(item: RegionalLeaseReturnWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnWindowStore(
    var items: MutableMap<String, RegionalLeaseReturnWindow>,
    var order: MutableList<String>,
) : RegionalLeaseReturnWindowStore {
    override fun find(code: String): RegionalLeaseReturnWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnWindow) {
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

    override fun list(): MutableList<RegionalLeaseReturnWindow> {
        val out = mutableListOf<RegionalLeaseReturnWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnWindowStore(): RegionalLeaseReturnWindowStore {
    return MemoryRegionalLeaseReturnWindowStore(
        mutableMapOf<String, RegionalLeaseReturnWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnWindowStore(
    var inner: RegionalLeaseReturnWindowStore,
    var cache: TtlCache<RegionalLeaseReturnWindow>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnWindowStore {
    override fun find(code: String): RegionalLeaseReturnWindow? {
        val hit = (cache.get(code) as RegionalLeaseReturnWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnWindow> = inner.list()
}

fun newCachedRegionalLeaseReturnWindowStore(inner: RegionalLeaseReturnWindowStore, ttl: Int, clock: Clock): RegionalLeaseReturnWindowStore {
    return CachedRegionalLeaseReturnWindowStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return windows. Empty fields match all. */
data class RegionalLeaseReturnWindowQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalLeaseReturnWindow>): MutableList<RegionalLeaseReturnWindow> {
        val matched = filter(items) { candidate: RegionalLeaseReturnWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnWindows(store: RegionalLeaseReturnWindowStore, query: RegionalLeaseReturnWindowQuery): MutableList<RegionalLeaseReturnWindow> {
    return query.apply(store.list())
}
