package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge ledgers by code. */
interface FuelSurchargeLedgerStore {
    fun find(code: String): FuelSurchargeLedger?
    fun save(item: FuelSurchargeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeLedgerStore(
    var items: MutableMap<String, FuelSurchargeLedger>,
    var order: MutableList<String>,
) : FuelSurchargeLedgerStore {
    override fun find(code: String): FuelSurchargeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeLedger) {
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

    override fun list(): MutableList<FuelSurchargeLedger> {
        val out = mutableListOf<FuelSurchargeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeLedgerStore(): FuelSurchargeLedgerStore {
    return MemoryFuelSurchargeLedgerStore(mutableMapOf<String, FuelSurchargeLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeLedgerStore(
    var inner: FuelSurchargeLedgerStore,
    var cache: TtlCache<FuelSurchargeLedger>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeLedgerStore {
    override fun find(code: String): FuelSurchargeLedger? {
        val hit = (cache.get(code) as FuelSurchargeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeLedger> = inner.list()
}

fun newCachedFuelSurchargeLedgerStore(inner: FuelSurchargeLedgerStore, ttl: Int, clock: Clock): FuelSurchargeLedgerStore {
    return CachedFuelSurchargeLedgerStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge ledgers. Empty fields match all. */
data class FuelSurchargeLedgerQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeLedger): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<FuelSurchargeLedger>): MutableList<FuelSurchargeLedger> {
        val matched = filter(items) { candidate: FuelSurchargeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeLedgers(store: FuelSurchargeLedgerStore, query: FuelSurchargeLedgerQuery): MutableList<FuelSurchargeLedger> {
    return query.apply(store.list())
}
