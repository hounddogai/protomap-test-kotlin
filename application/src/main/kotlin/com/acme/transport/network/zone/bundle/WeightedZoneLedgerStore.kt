package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted zone ledgers by code. */
interface WeightedZoneLedgerStore {
    fun find(code: String): WeightedZoneLedger?
    fun save(item: WeightedZoneLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedZoneLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedZoneLedgerStore(
    var items: MutableMap<String, WeightedZoneLedger>,
    var order: MutableList<String>,
) : WeightedZoneLedgerStore {
    override fun find(code: String): WeightedZoneLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedZoneLedger) {
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

    override fun list(): MutableList<WeightedZoneLedger> {
        val out = mutableListOf<WeightedZoneLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedZoneLedgerStore(): WeightedZoneLedgerStore {
    return MemoryWeightedZoneLedgerStore(mutableMapOf<String, WeightedZoneLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedZoneLedgerStore(
    var inner: WeightedZoneLedgerStore,
    var cache: TtlCache<WeightedZoneLedger>,
    var hits: Int,
    var misses: Int,
) : WeightedZoneLedgerStore {
    override fun find(code: String): WeightedZoneLedger? {
        val hit = (cache.get(code) as WeightedZoneLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedZoneLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedZoneLedger> = inner.list()
}

fun newCachedWeightedZoneLedgerStore(inner: WeightedZoneLedgerStore, ttl: Int, clock: Clock): WeightedZoneLedgerStore {
    return CachedWeightedZoneLedgerStore(
        inner = inner,
        cache = newTtlCache<WeightedZoneLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted zone ledgers. Empty fields match all. */
data class WeightedZoneLedgerQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedZoneLedger): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedZoneLedger>): MutableList<WeightedZoneLedger> {
        val matched = filter(items) { candidate: WeightedZoneLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedZoneLedgers(store: WeightedZoneLedgerStore, query: WeightedZoneLedgerQuery): MutableList<WeightedZoneLedger> {
    return query.apply(store.list())
}
