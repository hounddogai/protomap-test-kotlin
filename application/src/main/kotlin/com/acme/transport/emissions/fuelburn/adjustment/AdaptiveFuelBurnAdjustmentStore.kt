package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive fuel burn adjustments by code. */
interface AdaptiveFuelBurnAdjustmentStore {
    fun find(code: String): AdaptiveFuelBurnAdjustment?
    fun save(item: AdaptiveFuelBurnAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFuelBurnAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFuelBurnAdjustmentStore(
    var items: MutableMap<String, AdaptiveFuelBurnAdjustment>,
    var order: MutableList<String>,
) : AdaptiveFuelBurnAdjustmentStore {
    override fun find(code: String): AdaptiveFuelBurnAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFuelBurnAdjustment) {
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

    override fun list(): MutableList<AdaptiveFuelBurnAdjustment> {
        val out = mutableListOf<AdaptiveFuelBurnAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFuelBurnAdjustmentStore(): AdaptiveFuelBurnAdjustmentStore {
    return MemoryAdaptiveFuelBurnAdjustmentStore(
        mutableMapOf<String, AdaptiveFuelBurnAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFuelBurnAdjustmentStore(
    var inner: AdaptiveFuelBurnAdjustmentStore,
    var cache: TtlCache<AdaptiveFuelBurnAdjustment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFuelBurnAdjustmentStore {
    override fun find(code: String): AdaptiveFuelBurnAdjustment? {
        val hit = (cache.get(code) as AdaptiveFuelBurnAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFuelBurnAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFuelBurnAdjustment> = inner.list()
}

fun newCachedAdaptiveFuelBurnAdjustmentStore(inner: AdaptiveFuelBurnAdjustmentStore, ttl: Int, clock: Clock): AdaptiveFuelBurnAdjustmentStore {
    return CachedAdaptiveFuelBurnAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFuelBurnAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive fuel burn adjustments. Empty fields match all. */
data class AdaptiveFuelBurnAdjustmentQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFuelBurnAdjustment): Boolean {
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

    fun apply(items: MutableList<AdaptiveFuelBurnAdjustment>): MutableList<AdaptiveFuelBurnAdjustment> {
        val matched = filter(items) { candidate: AdaptiveFuelBurnAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFuelBurnAdjustments(store: AdaptiveFuelBurnAdjustmentStore, query: AdaptiveFuelBurnAdjustmentQuery): MutableList<AdaptiveFuelBurnAdjustment> {
    return query.apply(store.list())
}
