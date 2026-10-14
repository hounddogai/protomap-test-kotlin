package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive dispute rollups by code. */
interface AdaptiveDisputeRollupStore {
    fun find(code: String): AdaptiveDisputeRollup?
    fun save(item: AdaptiveDisputeRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDisputeRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDisputeRollupStore(
    var items: MutableMap<String, AdaptiveDisputeRollup>,
    var order: MutableList<String>,
) : AdaptiveDisputeRollupStore {
    override fun find(code: String): AdaptiveDisputeRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDisputeRollup) {
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

    override fun list(): MutableList<AdaptiveDisputeRollup> {
        val out = mutableListOf<AdaptiveDisputeRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDisputeRollupStore(): AdaptiveDisputeRollupStore {
    return MemoryAdaptiveDisputeRollupStore(
        mutableMapOf<String, AdaptiveDisputeRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDisputeRollupStore(
    var inner: AdaptiveDisputeRollupStore,
    var cache: TtlCache<AdaptiveDisputeRollup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDisputeRollupStore {
    override fun find(code: String): AdaptiveDisputeRollup? {
        val hit = (cache.get(code) as AdaptiveDisputeRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDisputeRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDisputeRollup> = inner.list()
}

fun newCachedAdaptiveDisputeRollupStore(inner: AdaptiveDisputeRollupStore, ttl: Int, clock: Clock): AdaptiveDisputeRollupStore {
    return CachedAdaptiveDisputeRollupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDisputeRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dispute rollups. Empty fields match all. */
data class AdaptiveDisputeRollupQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDisputeRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDisputeRollup>): MutableList<AdaptiveDisputeRollup> {
        val matched = filter(items) { candidate: AdaptiveDisputeRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDisputeRollups(store: AdaptiveDisputeRollupStore, query: AdaptiveDisputeRollupQuery): MutableList<AdaptiveDisputeRollup> {
    return query.apply(store.list())
}
