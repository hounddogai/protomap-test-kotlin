package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset rules by code. */
interface RouteOffsetRuleStore {
    fun find(code: String): RouteOffsetRule?
    fun save(item: RouteOffsetRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetRuleStore(
    var items: MutableMap<String, RouteOffsetRule>,
    var order: MutableList<String>,
) : RouteOffsetRuleStore {
    override fun find(code: String): RouteOffsetRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetRule) {
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

    override fun list(): MutableList<RouteOffsetRule> {
        val out = mutableListOf<RouteOffsetRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetRuleStore(): RouteOffsetRuleStore {
    return MemoryRouteOffsetRuleStore(mutableMapOf<String, RouteOffsetRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetRuleStore(
    var inner: RouteOffsetRuleStore,
    var cache: TtlCache<RouteOffsetRule>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetRuleStore {
    override fun find(code: String): RouteOffsetRule? {
        val hit = (cache.get(code) as RouteOffsetRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetRule> = inner.list()
}

fun newCachedRouteOffsetRuleStore(inner: RouteOffsetRuleStore, ttl: Int, clock: Clock): RouteOffsetRuleStore {
    return CachedRouteOffsetRuleStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset rules. Empty fields match all. */
data class RouteOffsetRuleQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetRule>): MutableList<RouteOffsetRule> {
        val matched = filter(items) { candidate: RouteOffsetRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetRules(store: RouteOffsetRuleStore, query: RouteOffsetRuleQuery): MutableList<RouteOffsetRule> {
    return query.apply(store.list())
}
