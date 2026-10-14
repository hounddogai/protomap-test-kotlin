package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive hold ledgers by code. */
interface AdaptiveHoldLedgerStore {
    fun find(code: String): AdaptiveHoldLedger?
    fun save(item: AdaptiveHoldLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveHoldLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveHoldLedgerStore(
    var items: MutableMap<String, AdaptiveHoldLedger>,
    var order: MutableList<String>,
) : AdaptiveHoldLedgerStore {
    override fun find(code: String): AdaptiveHoldLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveHoldLedger) {
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

    override fun list(): MutableList<AdaptiveHoldLedger> {
        val out = mutableListOf<AdaptiveHoldLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveHoldLedgerStore(): AdaptiveHoldLedgerStore {
    return MemoryAdaptiveHoldLedgerStore(mutableMapOf<String, AdaptiveHoldLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveHoldLedgerStore(
    var inner: AdaptiveHoldLedgerStore,
    var cache: TtlCache<AdaptiveHoldLedger>,
    var hits: Int,
    var misses: Int,
) : AdaptiveHoldLedgerStore {
    override fun find(code: String): AdaptiveHoldLedger? {
        val hit = (cache.get(code) as AdaptiveHoldLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveHoldLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveHoldLedger> = inner.list()
}

fun newCachedAdaptiveHoldLedgerStore(inner: AdaptiveHoldLedgerStore, ttl: Int, clock: Clock): AdaptiveHoldLedgerStore {
    return CachedAdaptiveHoldLedgerStore(
        inner = inner,
        cache = newTtlCache<AdaptiveHoldLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive hold ledgers. Empty fields match all. */
data class AdaptiveHoldLedgerQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveHoldLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveHoldLedger>): MutableList<AdaptiveHoldLedger> {
        val matched = filter(items) { candidate: AdaptiveHoldLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveHoldLedgers(store: AdaptiveHoldLedgerStore, query: AdaptiveHoldLedgerQuery): MutableList<AdaptiveHoldLedger> {
    return query.apply(store.list())
}
