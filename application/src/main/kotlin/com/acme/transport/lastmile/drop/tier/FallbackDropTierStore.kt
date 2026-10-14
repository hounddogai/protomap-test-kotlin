package com.acme.transport.lastmile.drop.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback drop tiers by code. */
interface FallbackDropTierStore {
    fun find(code: String): FallbackDropTier?
    fun save(item: FallbackDropTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDropTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDropTierStore(
    var items: MutableMap<String, FallbackDropTier>,
    var order: MutableList<String>,
) : FallbackDropTierStore {
    override fun find(code: String): FallbackDropTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDropTier) {
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

    override fun list(): MutableList<FallbackDropTier> {
        val out = mutableListOf<FallbackDropTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDropTierStore(): FallbackDropTierStore {
    return MemoryFallbackDropTierStore(mutableMapOf<String, FallbackDropTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDropTierStore(
    var inner: FallbackDropTierStore,
    var cache: TtlCache<FallbackDropTier>,
    var hits: Int,
    var misses: Int,
) : FallbackDropTierStore {
    override fun find(code: String): FallbackDropTier? {
        val hit = (cache.get(code) as FallbackDropTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDropTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDropTier> = inner.list()
}

fun newCachedFallbackDropTierStore(inner: FallbackDropTierStore, ttl: Int, clock: Clock): FallbackDropTierStore {
    return CachedFallbackDropTierStore(
        inner = inner,
        cache = newTtlCache<FallbackDropTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback drop tiers. Empty fields match all. */
data class FallbackDropTierQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDropTier): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDropTier>): MutableList<FallbackDropTier> {
        val matched = filter(items) { candidate: FallbackDropTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDropTiers(store: FallbackDropTierStore, query: FallbackDropTierQuery): MutableList<FallbackDropTier> {
    return query.apply(store.list())
}
