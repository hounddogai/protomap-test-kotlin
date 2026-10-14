package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire ledgers by code. */
interface TireLedgerStore {
    fun find(code: String): TireLedger?
    fun save(item: TireLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireLedgerStore(
    var items: MutableMap<String, TireLedger>,
    var order: MutableList<String>,
) : TireLedgerStore {
    override fun find(code: String): TireLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireLedger) {
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

    override fun list(): MutableList<TireLedger> {
        val out = mutableListOf<TireLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireLedgerStore(): TireLedgerStore {
    return MemoryTireLedgerStore(mutableMapOf<String, TireLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireLedgerStore(
    var inner: TireLedgerStore,
    var cache: TtlCache<TireLedger>,
    var hits: Int,
    var misses: Int,
) : TireLedgerStore {
    override fun find(code: String): TireLedger? {
        val hit = (cache.get(code) as TireLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireLedger> = inner.list()
}

fun newCachedTireLedgerStore(inner: TireLedgerStore, ttl: Int, clock: Clock): TireLedgerStore {
    return CachedTireLedgerStore(
        inner = inner,
        cache = newTtlCache<TireLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire ledgers. Empty fields match all. */
data class TireLedgerQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireLedger): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireLedger>): MutableList<TireLedger> {
        val matched = filter(items) { candidate: TireLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireLedgers(store: TireLedgerStore, query: TireLedgerQuery): MutableList<TireLedger> {
    return query.apply(store.list())
}
