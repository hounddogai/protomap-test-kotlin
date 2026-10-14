package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive accessorial adjustments by code. */
interface AdaptiveAccessorialAdjustmentStore {
    fun find(code: String): AdaptiveAccessorialAdjustment?
    fun save(item: AdaptiveAccessorialAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAccessorialAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAccessorialAdjustmentStore(
    var items: MutableMap<String, AdaptiveAccessorialAdjustment>,
    var order: MutableList<String>,
) : AdaptiveAccessorialAdjustmentStore {
    override fun find(code: String): AdaptiveAccessorialAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAccessorialAdjustment) {
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

    override fun list(): MutableList<AdaptiveAccessorialAdjustment> {
        val out = mutableListOf<AdaptiveAccessorialAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAccessorialAdjustmentStore(): AdaptiveAccessorialAdjustmentStore {
    return MemoryAdaptiveAccessorialAdjustmentStore(
        mutableMapOf<String, AdaptiveAccessorialAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAccessorialAdjustmentStore(
    var inner: AdaptiveAccessorialAdjustmentStore,
    var cache: TtlCache<AdaptiveAccessorialAdjustment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAccessorialAdjustmentStore {
    override fun find(code: String): AdaptiveAccessorialAdjustment? {
        val hit = (cache.get(code) as AdaptiveAccessorialAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAccessorialAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAccessorialAdjustment> = inner.list()
}

fun newCachedAdaptiveAccessorialAdjustmentStore(inner: AdaptiveAccessorialAdjustmentStore, ttl: Int, clock: Clock): AdaptiveAccessorialAdjustmentStore {
    return CachedAdaptiveAccessorialAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAccessorialAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive accessorial adjustments. Empty fields match all. */
data class AdaptiveAccessorialAdjustmentQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAccessorialAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAccessorialAdjustment>): MutableList<AdaptiveAccessorialAdjustment> {
        val matched = filter(items) { candidate: AdaptiveAccessorialAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAccessorialAdjustments(store: AdaptiveAccessorialAdjustmentStore, query: AdaptiveAccessorialAdjustmentQuery): MutableList<AdaptiveAccessorialAdjustment> {
    return query.apply(store.list())
}
