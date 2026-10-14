package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal zone policies by code. */
interface SeasonalZonePolicyStore {
    fun find(code: String): SeasonalZonePolicy?
    fun save(item: SeasonalZonePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalZonePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalZonePolicyStore(
    var items: MutableMap<String, SeasonalZonePolicy>,
    var order: MutableList<String>,
) : SeasonalZonePolicyStore {
    override fun find(code: String): SeasonalZonePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalZonePolicy) {
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

    override fun list(): MutableList<SeasonalZonePolicy> {
        val out = mutableListOf<SeasonalZonePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalZonePolicyStore(): SeasonalZonePolicyStore {
    return MemorySeasonalZonePolicyStore(mutableMapOf<String, SeasonalZonePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalZonePolicyStore(
    var inner: SeasonalZonePolicyStore,
    var cache: TtlCache<SeasonalZonePolicy>,
    var hits: Int,
    var misses: Int,
) : SeasonalZonePolicyStore {
    override fun find(code: String): SeasonalZonePolicy? {
        val hit = (cache.get(code) as SeasonalZonePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalZonePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalZonePolicy> = inner.list()
}

fun newCachedSeasonalZonePolicyStore(inner: SeasonalZonePolicyStore, ttl: Int, clock: Clock): SeasonalZonePolicyStore {
    return CachedSeasonalZonePolicyStore(
        inner = inner,
        cache = newTtlCache<SeasonalZonePolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal zone policies. Empty fields match all. */
data class SeasonalZonePolicyQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalZonePolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalZonePolicy>): MutableList<SeasonalZonePolicy> {
        val matched = filter(items) { candidate: SeasonalZonePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalZonePolicies(store: SeasonalZonePolicyStore, query: SeasonalZonePolicyQuery): MutableList<SeasonalZonePolicy> {
    return query.apply(store.list())
}
