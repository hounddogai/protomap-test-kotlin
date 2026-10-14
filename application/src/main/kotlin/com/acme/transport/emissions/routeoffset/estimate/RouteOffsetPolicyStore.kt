package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset policies by code. */
interface RouteOffsetPolicyStore {
    fun find(code: String): RouteOffsetPolicy?
    fun save(item: RouteOffsetPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetPolicyStore(
    var items: MutableMap<String, RouteOffsetPolicy>,
    var order: MutableList<String>,
) : RouteOffsetPolicyStore {
    override fun find(code: String): RouteOffsetPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetPolicy) {
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

    override fun list(): MutableList<RouteOffsetPolicy> {
        val out = mutableListOf<RouteOffsetPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetPolicyStore(): RouteOffsetPolicyStore {
    return MemoryRouteOffsetPolicyStore(mutableMapOf<String, RouteOffsetPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetPolicyStore(
    var inner: RouteOffsetPolicyStore,
    var cache: TtlCache<RouteOffsetPolicy>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetPolicyStore {
    override fun find(code: String): RouteOffsetPolicy? {
        val hit = (cache.get(code) as RouteOffsetPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetPolicy> = inner.list()
}

fun newCachedRouteOffsetPolicyStore(inner: RouteOffsetPolicyStore, ttl: Int, clock: Clock): RouteOffsetPolicyStore {
    return CachedRouteOffsetPolicyStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset policies. Empty fields match all. */
data class RouteOffsetPolicyQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetPolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetPolicy>): MutableList<RouteOffsetPolicy> {
        val matched = filter(items) { candidate: RouteOffsetPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetPolicies(store: RouteOffsetPolicyStore, query: RouteOffsetPolicyQuery): MutableList<RouteOffsetPolicy> {
    return query.apply(store.list())
}
