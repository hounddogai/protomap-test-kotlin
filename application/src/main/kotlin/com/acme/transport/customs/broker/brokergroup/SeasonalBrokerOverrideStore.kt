package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal broker overrides by code. */
interface SeasonalBrokerOverrideStore {
    fun find(code: String): SeasonalBrokerOverride?
    fun save(item: SeasonalBrokerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalBrokerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalBrokerOverrideStore(
    var items: MutableMap<String, SeasonalBrokerOverride>,
    var order: MutableList<String>,
) : SeasonalBrokerOverrideStore {
    override fun find(code: String): SeasonalBrokerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalBrokerOverride) {
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

    override fun list(): MutableList<SeasonalBrokerOverride> {
        val out = mutableListOf<SeasonalBrokerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalBrokerOverrideStore(): SeasonalBrokerOverrideStore {
    return MemorySeasonalBrokerOverrideStore(
        mutableMapOf<String, SeasonalBrokerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalBrokerOverrideStore(
    var inner: SeasonalBrokerOverrideStore,
    var cache: TtlCache<SeasonalBrokerOverride>,
    var hits: Int,
    var misses: Int,
) : SeasonalBrokerOverrideStore {
    override fun find(code: String): SeasonalBrokerOverride? {
        val hit = (cache.get(code) as SeasonalBrokerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalBrokerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalBrokerOverride> = inner.list()
}

fun newCachedSeasonalBrokerOverrideStore(inner: SeasonalBrokerOverrideStore, ttl: Int, clock: Clock): SeasonalBrokerOverrideStore {
    return CachedSeasonalBrokerOverrideStore(
        inner = inner,
        cache = newTtlCache<SeasonalBrokerOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal broker overrides. Empty fields match all. */
data class SeasonalBrokerOverrideQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalBrokerOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SeasonalBrokerOverride>): MutableList<SeasonalBrokerOverride> {
        val matched = filter(items) { candidate: SeasonalBrokerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalBrokerOverrides(store: SeasonalBrokerOverrideStore, query: SeasonalBrokerOverrideQuery): MutableList<SeasonalBrokerOverride> {
    return query.apply(store.list())
}
