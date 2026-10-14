package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary route offset ledgers by code. */
interface PrimaryRouteOffsetLedgerStore {
    fun find(code: String): PrimaryRouteOffsetLedger?
    fun save(item: PrimaryRouteOffsetLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRouteOffsetLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRouteOffsetLedgerStore(
    var items: MutableMap<String, PrimaryRouteOffsetLedger>,
    var order: MutableList<String>,
) : PrimaryRouteOffsetLedgerStore {
    override fun find(code: String): PrimaryRouteOffsetLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRouteOffsetLedger) {
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

    override fun list(): MutableList<PrimaryRouteOffsetLedger> {
        val out = mutableListOf<PrimaryRouteOffsetLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRouteOffsetLedgerStore(): PrimaryRouteOffsetLedgerStore {
    return MemoryPrimaryRouteOffsetLedgerStore(
        mutableMapOf<String, PrimaryRouteOffsetLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRouteOffsetLedgerStore(
    var inner: PrimaryRouteOffsetLedgerStore,
    var cache: TtlCache<PrimaryRouteOffsetLedger>,
    var hits: Int,
    var misses: Int,
) : PrimaryRouteOffsetLedgerStore {
    override fun find(code: String): PrimaryRouteOffsetLedger? {
        val hit = (cache.get(code) as PrimaryRouteOffsetLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRouteOffsetLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRouteOffsetLedger> = inner.list()
}

fun newCachedPrimaryRouteOffsetLedgerStore(inner: PrimaryRouteOffsetLedgerStore, ttl: Int, clock: Clock): PrimaryRouteOffsetLedgerStore {
    return CachedPrimaryRouteOffsetLedgerStore(
        inner = inner,
        cache = newTtlCache<PrimaryRouteOffsetLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary route offset ledgers. Empty fields match all. */
data class PrimaryRouteOffsetLedgerQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRouteOffsetLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRouteOffsetLedger>): MutableList<PrimaryRouteOffsetLedger> {
        val matched = filter(items) { candidate: PrimaryRouteOffsetLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRouteOffsetLedgers(store: PrimaryRouteOffsetLedgerStore, query: PrimaryRouteOffsetLedgerQuery): MutableList<PrimaryRouteOffsetLedger> {
    return query.apply(store.list())
}
