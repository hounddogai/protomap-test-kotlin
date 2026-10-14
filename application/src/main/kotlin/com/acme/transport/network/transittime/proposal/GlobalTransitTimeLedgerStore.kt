package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global transit time ledgers by code. */
interface GlobalTransitTimeLedgerStore {
    fun find(code: String): GlobalTransitTimeLedger?
    fun save(item: GlobalTransitTimeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTransitTimeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTransitTimeLedgerStore(
    var items: MutableMap<String, GlobalTransitTimeLedger>,
    var order: MutableList<String>,
) : GlobalTransitTimeLedgerStore {
    override fun find(code: String): GlobalTransitTimeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTransitTimeLedger) {
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

    override fun list(): MutableList<GlobalTransitTimeLedger> {
        val out = mutableListOf<GlobalTransitTimeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTransitTimeLedgerStore(): GlobalTransitTimeLedgerStore {
    return MemoryGlobalTransitTimeLedgerStore(
        mutableMapOf<String, GlobalTransitTimeLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTransitTimeLedgerStore(
    var inner: GlobalTransitTimeLedgerStore,
    var cache: TtlCache<GlobalTransitTimeLedger>,
    var hits: Int,
    var misses: Int,
) : GlobalTransitTimeLedgerStore {
    override fun find(code: String): GlobalTransitTimeLedger? {
        val hit = (cache.get(code) as GlobalTransitTimeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTransitTimeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTransitTimeLedger> = inner.list()
}

fun newCachedGlobalTransitTimeLedgerStore(inner: GlobalTransitTimeLedgerStore, ttl: Int, clock: Clock): GlobalTransitTimeLedgerStore {
    return CachedGlobalTransitTimeLedgerStore(
        inner = inner,
        cache = newTtlCache<GlobalTransitTimeLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global transit time ledgers. Empty fields match all. */
data class GlobalTransitTimeLedgerQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTransitTimeLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTransitTimeLedger>): MutableList<GlobalTransitTimeLedger> {
        val matched = filter(items) { candidate: GlobalTransitTimeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTransitTimeLedgers(store: GlobalTransitTimeLedgerStore, query: GlobalTransitTimeLedgerQuery): MutableList<GlobalTransitTimeLedger> {
    return query.apply(store.list())
}
