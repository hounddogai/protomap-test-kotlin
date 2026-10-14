package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive backhaul ledgers by code. */
interface AdaptiveBackhaulLedgerStore {
    fun find(code: String): AdaptiveBackhaulLedger?
    fun save(item: AdaptiveBackhaulLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBackhaulLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBackhaulLedgerStore(
    var items: MutableMap<String, AdaptiveBackhaulLedger>,
    var order: MutableList<String>,
) : AdaptiveBackhaulLedgerStore {
    override fun find(code: String): AdaptiveBackhaulLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBackhaulLedger) {
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

    override fun list(): MutableList<AdaptiveBackhaulLedger> {
        val out = mutableListOf<AdaptiveBackhaulLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBackhaulLedgerStore(): AdaptiveBackhaulLedgerStore {
    return MemoryAdaptiveBackhaulLedgerStore(
        mutableMapOf<String, AdaptiveBackhaulLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBackhaulLedgerStore(
    var inner: AdaptiveBackhaulLedgerStore,
    var cache: TtlCache<AdaptiveBackhaulLedger>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBackhaulLedgerStore {
    override fun find(code: String): AdaptiveBackhaulLedger? {
        val hit = (cache.get(code) as AdaptiveBackhaulLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBackhaulLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBackhaulLedger> = inner.list()
}

fun newCachedAdaptiveBackhaulLedgerStore(inner: AdaptiveBackhaulLedgerStore, ttl: Int, clock: Clock): AdaptiveBackhaulLedgerStore {
    return CachedAdaptiveBackhaulLedgerStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBackhaulLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive backhaul ledgers. Empty fields match all. */
data class AdaptiveBackhaulLedgerQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBackhaulLedger): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveBackhaulLedger>): MutableList<AdaptiveBackhaulLedger> {
        val matched = filter(items) { candidate: AdaptiveBackhaulLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBackhaulLedgers(store: AdaptiveBackhaulLedgerStore, query: AdaptiveBackhaulLedgerQuery): MutableList<AdaptiveBackhaulLedger> {
    return query.apply(store.list())
}
