package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard policies by code. */
interface SeasonalTrailerYardPolicyStore {
    fun find(code: String): SeasonalTrailerYardPolicy?
    fun save(item: SeasonalTrailerYardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardPolicyStore(
    var items: MutableMap<String, SeasonalTrailerYardPolicy>,
    var order: MutableList<String>,
) : SeasonalTrailerYardPolicyStore {
    override fun find(code: String): SeasonalTrailerYardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardPolicy) {
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

    override fun list(): MutableList<SeasonalTrailerYardPolicy> {
        val out = mutableListOf<SeasonalTrailerYardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardPolicyStore(): SeasonalTrailerYardPolicyStore {
    return MemorySeasonalTrailerYardPolicyStore(
        mutableMapOf<String, SeasonalTrailerYardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardPolicyStore(
    var inner: SeasonalTrailerYardPolicyStore,
    var cache: TtlCache<SeasonalTrailerYardPolicy>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardPolicyStore {
    override fun find(code: String): SeasonalTrailerYardPolicy? {
        val hit = (cache.get(code) as SeasonalTrailerYardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardPolicy> = inner.list()
}

fun newCachedSeasonalTrailerYardPolicyStore(inner: SeasonalTrailerYardPolicyStore, ttl: Int, clock: Clock): SeasonalTrailerYardPolicyStore {
    return CachedSeasonalTrailerYardPolicyStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard policies. Empty fields match all. */
data class SeasonalTrailerYardPolicyQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardPolicy): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardPolicy>): MutableList<SeasonalTrailerYardPolicy> {
        val matched = filter(items) { candidate: SeasonalTrailerYardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardPolicies(store: SeasonalTrailerYardPolicyStore, query: SeasonalTrailerYardPolicyQuery): MutableList<SeasonalTrailerYardPolicy> {
    return query.apply(store.list())
}
