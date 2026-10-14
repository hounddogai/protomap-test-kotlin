package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default hub tiers by code. */
interface DefaultHubTierStore {
    fun find(code: String): DefaultHubTier?
    fun save(item: DefaultHubTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultHubTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultHubTierStore(
    var items: MutableMap<String, DefaultHubTier>,
    var order: MutableList<String>,
) : DefaultHubTierStore {
    override fun find(code: String): DefaultHubTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultHubTier) {
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

    override fun list(): MutableList<DefaultHubTier> {
        val out = mutableListOf<DefaultHubTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultHubTierStore(): DefaultHubTierStore {
    return MemoryDefaultHubTierStore(mutableMapOf<String, DefaultHubTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultHubTierStore(
    var inner: DefaultHubTierStore,
    var cache: TtlCache<DefaultHubTier>,
    var hits: Int,
    var misses: Int,
) : DefaultHubTierStore {
    override fun find(code: String): DefaultHubTier? {
        val hit = (cache.get(code) as DefaultHubTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultHubTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultHubTier> = inner.list()
}

fun newCachedDefaultHubTierStore(inner: DefaultHubTierStore, ttl: Int, clock: Clock): DefaultHubTierStore {
    return CachedDefaultHubTierStore(
        inner = inner,
        cache = newTtlCache<DefaultHubTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default hub tiers. Empty fields match all. */
data class DefaultHubTierQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultHubTier): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultHubTier>): MutableList<DefaultHubTier> {
        val matched = filter(items) { candidate: DefaultHubTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultHubTiers(store: DefaultHubTierStore, query: DefaultHubTierQuery): MutableList<DefaultHubTier> {
    return query.apply(store.list())
}
