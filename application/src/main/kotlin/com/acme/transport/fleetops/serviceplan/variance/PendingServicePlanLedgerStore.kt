package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending service plan ledgers by code. */
interface PendingServicePlanLedgerStore {
    fun find(code: String): PendingServicePlanLedger?
    fun save(item: PendingServicePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServicePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServicePlanLedgerStore(
    var items: MutableMap<String, PendingServicePlanLedger>,
    var order: MutableList<String>,
) : PendingServicePlanLedgerStore {
    override fun find(code: String): PendingServicePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServicePlanLedger) {
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

    override fun list(): MutableList<PendingServicePlanLedger> {
        val out = mutableListOf<PendingServicePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServicePlanLedgerStore(): PendingServicePlanLedgerStore {
    return MemoryPendingServicePlanLedgerStore(
        mutableMapOf<String, PendingServicePlanLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServicePlanLedgerStore(
    var inner: PendingServicePlanLedgerStore,
    var cache: TtlCache<PendingServicePlanLedger>,
    var hits: Int,
    var misses: Int,
) : PendingServicePlanLedgerStore {
    override fun find(code: String): PendingServicePlanLedger? {
        val hit = (cache.get(code) as PendingServicePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServicePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServicePlanLedger> = inner.list()
}

fun newCachedPendingServicePlanLedgerStore(inner: PendingServicePlanLedgerStore, ttl: Int, clock: Clock): PendingServicePlanLedgerStore {
    return CachedPendingServicePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<PendingServicePlanLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service plan ledgers. Empty fields match all. */
data class PendingServicePlanLedgerQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServicePlanLedger): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServicePlanLedger>): MutableList<PendingServicePlanLedger> {
        val matched = filter(items) { candidate: PendingServicePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServicePlanLedgers(store: PendingServicePlanLedgerStore, query: PendingServicePlanLedgerQuery): MutableList<PendingServicePlanLedger> {
    return query.apply(store.list())
}
