package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive lease return tiers by code. */
interface AdaptiveLeaseReturnTierStore {
    fun find(code: String): AdaptiveLeaseReturnTier?
    fun save(item: AdaptiveLeaseReturnTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLeaseReturnTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLeaseReturnTierStore(
    var items: MutableMap<String, AdaptiveLeaseReturnTier>,
    var order: MutableList<String>,
) : AdaptiveLeaseReturnTierStore {
    override fun find(code: String): AdaptiveLeaseReturnTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLeaseReturnTier) {
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

    override fun list(): MutableList<AdaptiveLeaseReturnTier> {
        val out = mutableListOf<AdaptiveLeaseReturnTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLeaseReturnTierStore(): AdaptiveLeaseReturnTierStore {
    return MemoryAdaptiveLeaseReturnTierStore(
        mutableMapOf<String, AdaptiveLeaseReturnTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLeaseReturnTierStore(
    var inner: AdaptiveLeaseReturnTierStore,
    var cache: TtlCache<AdaptiveLeaseReturnTier>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLeaseReturnTierStore {
    override fun find(code: String): AdaptiveLeaseReturnTier? {
        val hit = (cache.get(code) as AdaptiveLeaseReturnTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLeaseReturnTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLeaseReturnTier> = inner.list()
}

fun newCachedAdaptiveLeaseReturnTierStore(inner: AdaptiveLeaseReturnTierStore, ttl: Int, clock: Clock): AdaptiveLeaseReturnTierStore {
    return CachedAdaptiveLeaseReturnTierStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLeaseReturnTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lease return tiers. Empty fields match all. */
data class AdaptiveLeaseReturnTierQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLeaseReturnTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLeaseReturnTier>): MutableList<AdaptiveLeaseReturnTier> {
        val matched = filter(items) { candidate: AdaptiveLeaseReturnTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLeaseReturnTiers(store: AdaptiveLeaseReturnTierStore, query: AdaptiveLeaseReturnTierQuery): MutableList<AdaptiveLeaseReturnTier> {
    return query.apply(store.list())
}
