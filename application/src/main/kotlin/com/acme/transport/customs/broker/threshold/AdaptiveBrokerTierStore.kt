package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive broker tiers by code. */
interface AdaptiveBrokerTierStore {
    fun find(code: String): AdaptiveBrokerTier?
    fun save(item: AdaptiveBrokerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBrokerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBrokerTierStore(
    var items: MutableMap<String, AdaptiveBrokerTier>,
    var order: MutableList<String>,
) : AdaptiveBrokerTierStore {
    override fun find(code: String): AdaptiveBrokerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBrokerTier) {
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

    override fun list(): MutableList<AdaptiveBrokerTier> {
        val out = mutableListOf<AdaptiveBrokerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBrokerTierStore(): AdaptiveBrokerTierStore {
    return MemoryAdaptiveBrokerTierStore(mutableMapOf<String, AdaptiveBrokerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBrokerTierStore(
    var inner: AdaptiveBrokerTierStore,
    var cache: TtlCache<AdaptiveBrokerTier>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBrokerTierStore {
    override fun find(code: String): AdaptiveBrokerTier? {
        val hit = (cache.get(code) as AdaptiveBrokerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBrokerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBrokerTier> = inner.list()
}

fun newCachedAdaptiveBrokerTierStore(inner: AdaptiveBrokerTierStore, ttl: Int, clock: Clock): AdaptiveBrokerTierStore {
    return CachedAdaptiveBrokerTierStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBrokerTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive broker tiers. Empty fields match all. */
data class AdaptiveBrokerTierQuery(
    var country: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBrokerTier): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveBrokerTier>): MutableList<AdaptiveBrokerTier> {
        val matched = filter(items) { candidate: AdaptiveBrokerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBrokerTiers(store: AdaptiveBrokerTierStore, query: AdaptiveBrokerTierQuery): MutableList<AdaptiveBrokerTier> {
    return query.apply(store.list())
}
