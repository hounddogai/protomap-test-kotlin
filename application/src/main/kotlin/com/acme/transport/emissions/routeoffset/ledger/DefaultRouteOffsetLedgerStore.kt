package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default route offset ledgers by code. */
interface DefaultRouteOffsetLedgerStore {
    fun find(code: String): DefaultRouteOffsetLedger?
    fun save(item: DefaultRouteOffsetLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRouteOffsetLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRouteOffsetLedgerStore(
    var items: MutableMap<String, DefaultRouteOffsetLedger>,
    var order: MutableList<String>,
) : DefaultRouteOffsetLedgerStore {
    override fun find(code: String): DefaultRouteOffsetLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRouteOffsetLedger) {
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

    override fun list(): MutableList<DefaultRouteOffsetLedger> {
        val out = mutableListOf<DefaultRouteOffsetLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRouteOffsetLedgerStore(): DefaultRouteOffsetLedgerStore {
    return MemoryDefaultRouteOffsetLedgerStore(
        mutableMapOf<String, DefaultRouteOffsetLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRouteOffsetLedgerStore(
    var inner: DefaultRouteOffsetLedgerStore,
    var cache: TtlCache<DefaultRouteOffsetLedger>,
    var hits: Int,
    var misses: Int,
) : DefaultRouteOffsetLedgerStore {
    override fun find(code: String): DefaultRouteOffsetLedger? {
        val hit = (cache.get(code) as DefaultRouteOffsetLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRouteOffsetLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRouteOffsetLedger> = inner.list()
}

fun newCachedDefaultRouteOffsetLedgerStore(inner: DefaultRouteOffsetLedgerStore, ttl: Int, clock: Clock): DefaultRouteOffsetLedgerStore {
    return CachedDefaultRouteOffsetLedgerStore(
        inner = inner,
        cache = newTtlCache<DefaultRouteOffsetLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default route offset ledgers. Empty fields match all. */
data class DefaultRouteOffsetLedgerQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRouteOffsetLedger): Boolean {
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

    fun apply(items: MutableList<DefaultRouteOffsetLedger>): MutableList<DefaultRouteOffsetLedger> {
        val matched = filter(items) { candidate: DefaultRouteOffsetLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRouteOffsetLedgers(store: DefaultRouteOffsetLedgerStore, query: DefaultRouteOffsetLedgerQuery): MutableList<DefaultRouteOffsetLedger> {
    return query.apply(store.list())
}
