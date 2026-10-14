package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback service plan ledgers by code. */
interface FallbackServicePlanLedgerStore {
    fun find(code: String): FallbackServicePlanLedger?
    fun save(item: FallbackServicePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServicePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServicePlanLedgerStore(
    var items: MutableMap<String, FallbackServicePlanLedger>,
    var order: MutableList<String>,
) : FallbackServicePlanLedgerStore {
    override fun find(code: String): FallbackServicePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServicePlanLedger) {
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

    override fun list(): MutableList<FallbackServicePlanLedger> {
        val out = mutableListOf<FallbackServicePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServicePlanLedgerStore(): FallbackServicePlanLedgerStore {
    return MemoryFallbackServicePlanLedgerStore(
        mutableMapOf<String, FallbackServicePlanLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServicePlanLedgerStore(
    var inner: FallbackServicePlanLedgerStore,
    var cache: TtlCache<FallbackServicePlanLedger>,
    var hits: Int,
    var misses: Int,
) : FallbackServicePlanLedgerStore {
    override fun find(code: String): FallbackServicePlanLedger? {
        val hit = (cache.get(code) as FallbackServicePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServicePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServicePlanLedger> = inner.list()
}

fun newCachedFallbackServicePlanLedgerStore(inner: FallbackServicePlanLedgerStore, ttl: Int, clock: Clock): FallbackServicePlanLedgerStore {
    return CachedFallbackServicePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<FallbackServicePlanLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service plan ledgers. Empty fields match all. */
data class FallbackServicePlanLedgerQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServicePlanLedger): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackServicePlanLedger>): MutableList<FallbackServicePlanLedger> {
        val matched = filter(items) { candidate: FallbackServicePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServicePlanLedgers(store: FallbackServicePlanLedgerStore, query: FallbackServicePlanLedgerQuery): MutableList<FallbackServicePlanLedger> {
    return query.apply(store.list())
}
