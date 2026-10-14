package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted ev charge ledgers by code. */
interface WeightedEvChargeLedgerStore {
    fun find(code: String): WeightedEvChargeLedger?
    fun save(item: WeightedEvChargeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEvChargeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEvChargeLedgerStore(
    var items: MutableMap<String, WeightedEvChargeLedger>,
    var order: MutableList<String>,
) : WeightedEvChargeLedgerStore {
    override fun find(code: String): WeightedEvChargeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEvChargeLedger) {
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

    override fun list(): MutableList<WeightedEvChargeLedger> {
        val out = mutableListOf<WeightedEvChargeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEvChargeLedgerStore(): WeightedEvChargeLedgerStore {
    return MemoryWeightedEvChargeLedgerStore(
        mutableMapOf<String, WeightedEvChargeLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEvChargeLedgerStore(
    var inner: WeightedEvChargeLedgerStore,
    var cache: TtlCache<WeightedEvChargeLedger>,
    var hits: Int,
    var misses: Int,
) : WeightedEvChargeLedgerStore {
    override fun find(code: String): WeightedEvChargeLedger? {
        val hit = (cache.get(code) as WeightedEvChargeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEvChargeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEvChargeLedger> = inner.list()
}

fun newCachedWeightedEvChargeLedgerStore(inner: WeightedEvChargeLedgerStore, ttl: Int, clock: Clock): WeightedEvChargeLedgerStore {
    return CachedWeightedEvChargeLedgerStore(
        inner = inner,
        cache = newTtlCache<WeightedEvChargeLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted ev charge ledgers. Empty fields match all. */
data class WeightedEvChargeLedgerQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEvChargeLedger): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedEvChargeLedger>): MutableList<WeightedEvChargeLedger> {
        val matched = filter(items) { candidate: WeightedEvChargeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEvChargeLedgers(store: WeightedEvChargeLedgerStore, query: WeightedEvChargeLedgerQuery): MutableList<WeightedEvChargeLedger> {
    return query.apply(store.list())
}
