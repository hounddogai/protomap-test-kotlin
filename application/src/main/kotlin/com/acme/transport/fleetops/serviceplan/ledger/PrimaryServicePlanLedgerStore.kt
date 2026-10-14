package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary service plan ledgers by code. */
interface PrimaryServicePlanLedgerStore {
    fun find(code: String): PrimaryServicePlanLedger?
    fun save(item: PrimaryServicePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryServicePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryServicePlanLedgerStore(
    var items: MutableMap<String, PrimaryServicePlanLedger>,
    var order: MutableList<String>,
) : PrimaryServicePlanLedgerStore {
    override fun find(code: String): PrimaryServicePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryServicePlanLedger) {
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

    override fun list(): MutableList<PrimaryServicePlanLedger> {
        val out = mutableListOf<PrimaryServicePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryServicePlanLedgerStore(): PrimaryServicePlanLedgerStore {
    return MemoryPrimaryServicePlanLedgerStore(
        mutableMapOf<String, PrimaryServicePlanLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryServicePlanLedgerStore(
    var inner: PrimaryServicePlanLedgerStore,
    var cache: TtlCache<PrimaryServicePlanLedger>,
    var hits: Int,
    var misses: Int,
) : PrimaryServicePlanLedgerStore {
    override fun find(code: String): PrimaryServicePlanLedger? {
        val hit = (cache.get(code) as PrimaryServicePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryServicePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryServicePlanLedger> = inner.list()
}

fun newCachedPrimaryServicePlanLedgerStore(inner: PrimaryServicePlanLedgerStore, ttl: Int, clock: Clock): PrimaryServicePlanLedgerStore {
    return CachedPrimaryServicePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<PrimaryServicePlanLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary service plan ledgers. Empty fields match all. */
data class PrimaryServicePlanLedgerQuery(
    var region: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryServicePlanLedger): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<PrimaryServicePlanLedger>): MutableList<PrimaryServicePlanLedger> {
        val matched = filter(items) { candidate: PrimaryServicePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryServicePlanLedgers(store: PrimaryServicePlanLedgerStore, query: PrimaryServicePlanLedgerQuery): MutableList<PrimaryServicePlanLedger> {
    return query.apply(store.list())
}
