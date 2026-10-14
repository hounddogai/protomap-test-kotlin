package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global route offset cycles by code. */
interface GlobalRouteOffsetCycleStore {
    fun find(code: String): GlobalRouteOffsetCycle?
    fun save(item: GlobalRouteOffsetCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRouteOffsetCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRouteOffsetCycleStore(
    var items: MutableMap<String, GlobalRouteOffsetCycle>,
    var order: MutableList<String>,
) : GlobalRouteOffsetCycleStore {
    override fun find(code: String): GlobalRouteOffsetCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRouteOffsetCycle) {
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

    override fun list(): MutableList<GlobalRouteOffsetCycle> {
        val out = mutableListOf<GlobalRouteOffsetCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRouteOffsetCycleStore(): GlobalRouteOffsetCycleStore {
    return MemoryGlobalRouteOffsetCycleStore(
        mutableMapOf<String, GlobalRouteOffsetCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRouteOffsetCycleStore(
    var inner: GlobalRouteOffsetCycleStore,
    var cache: TtlCache<GlobalRouteOffsetCycle>,
    var hits: Int,
    var misses: Int,
) : GlobalRouteOffsetCycleStore {
    override fun find(code: String): GlobalRouteOffsetCycle? {
        val hit = (cache.get(code) as GlobalRouteOffsetCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRouteOffsetCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRouteOffsetCycle> = inner.list()
}

fun newCachedGlobalRouteOffsetCycleStore(inner: GlobalRouteOffsetCycleStore, ttl: Int, clock: Clock): GlobalRouteOffsetCycleStore {
    return CachedGlobalRouteOffsetCycleStore(
        inner = inner,
        cache = newTtlCache<GlobalRouteOffsetCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global route offset cycles. Empty fields match all. */
data class GlobalRouteOffsetCycleQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRouteOffsetCycle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<GlobalRouteOffsetCycle>): MutableList<GlobalRouteOffsetCycle> {
        val matched = filter(items) { candidate: GlobalRouteOffsetCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRouteOffsetCycles(store: GlobalRouteOffsetCycleStore, query: GlobalRouteOffsetCycleQuery): MutableList<GlobalRouteOffsetCycle> {
    return query.apply(store.list())
}
