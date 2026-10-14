package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset ledgers by code. */
interface RouteOffsetLedgerStore {
    fun find(code: String): RouteOffsetLedger?
    fun save(item: RouteOffsetLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetLedgerStore(
    var items: MutableMap<String, RouteOffsetLedger>,
    var order: MutableList<String>,
) : RouteOffsetLedgerStore {
    override fun find(code: String): RouteOffsetLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetLedger) {
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

    override fun list(): MutableList<RouteOffsetLedger> {
        val out = mutableListOf<RouteOffsetLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetLedgerStore(): RouteOffsetLedgerStore {
    return MemoryRouteOffsetLedgerStore(mutableMapOf<String, RouteOffsetLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetLedgerStore(
    var inner: RouteOffsetLedgerStore,
    var cache: TtlCache<RouteOffsetLedger>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetLedgerStore {
    override fun find(code: String): RouteOffsetLedger? {
        val hit = (cache.get(code) as RouteOffsetLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetLedger> = inner.list()
}

fun newCachedRouteOffsetLedgerStore(inner: RouteOffsetLedgerStore, ttl: Int, clock: Clock): RouteOffsetLedgerStore {
    return CachedRouteOffsetLedgerStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset ledgers. Empty fields match all. */
data class RouteOffsetLedgerQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetLedger>): MutableList<RouteOffsetLedger> {
        val matched = filter(items) { candidate: RouteOffsetLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetLedgers(store: RouteOffsetLedgerStore, query: RouteOffsetLedgerQuery): MutableList<RouteOffsetLedger> {
    return query.apply(store.list())
}
