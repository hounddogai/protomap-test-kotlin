package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset rules by code. */
interface FallbackRouteOffsetRuleStore {
    fun find(code: String): FallbackRouteOffsetRule?
    fun save(item: FallbackRouteOffsetRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetRuleStore(
    var items: MutableMap<String, FallbackRouteOffsetRule>,
    var order: MutableList<String>,
) : FallbackRouteOffsetRuleStore {
    override fun find(code: String): FallbackRouteOffsetRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetRule) {
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

    override fun list(): MutableList<FallbackRouteOffsetRule> {
        val out = mutableListOf<FallbackRouteOffsetRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetRuleStore(): FallbackRouteOffsetRuleStore {
    return MemoryFallbackRouteOffsetRuleStore(
        mutableMapOf<String, FallbackRouteOffsetRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetRuleStore(
    var inner: FallbackRouteOffsetRuleStore,
    var cache: TtlCache<FallbackRouteOffsetRule>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetRuleStore {
    override fun find(code: String): FallbackRouteOffsetRule? {
        val hit = (cache.get(code) as FallbackRouteOffsetRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetRule> = inner.list()
}

fun newCachedFallbackRouteOffsetRuleStore(inner: FallbackRouteOffsetRuleStore, ttl: Int, clock: Clock): FallbackRouteOffsetRuleStore {
    return CachedFallbackRouteOffsetRuleStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset rules. Empty fields match all. */
data class FallbackRouteOffsetRuleQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetRule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<FallbackRouteOffsetRule>): MutableList<FallbackRouteOffsetRule> {
        val matched = filter(items) { candidate: FallbackRouteOffsetRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetRules(store: FallbackRouteOffsetRuleStore, query: FallbackRouteOffsetRuleQuery): MutableList<FallbackRouteOffsetRule> {
    return query.apply(store.list())
}
