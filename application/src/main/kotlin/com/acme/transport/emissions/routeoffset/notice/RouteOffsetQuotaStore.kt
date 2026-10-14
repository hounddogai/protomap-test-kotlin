package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset quotas by code. */
interface RouteOffsetQuotaStore {
    fun find(code: String): RouteOffsetQuota?
    fun save(item: RouteOffsetQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetQuotaStore(
    var items: MutableMap<String, RouteOffsetQuota>,
    var order: MutableList<String>,
) : RouteOffsetQuotaStore {
    override fun find(code: String): RouteOffsetQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetQuota) {
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

    override fun list(): MutableList<RouteOffsetQuota> {
        val out = mutableListOf<RouteOffsetQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetQuotaStore(): RouteOffsetQuotaStore {
    return MemoryRouteOffsetQuotaStore(mutableMapOf<String, RouteOffsetQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetQuotaStore(
    var inner: RouteOffsetQuotaStore,
    var cache: TtlCache<RouteOffsetQuota>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetQuotaStore {
    override fun find(code: String): RouteOffsetQuota? {
        val hit = (cache.get(code) as RouteOffsetQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetQuota> = inner.list()
}

fun newCachedRouteOffsetQuotaStore(inner: RouteOffsetQuotaStore, ttl: Int, clock: Clock): RouteOffsetQuotaStore {
    return CachedRouteOffsetQuotaStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset quotas. Empty fields match all. */
data class RouteOffsetQuotaQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetQuota>): MutableList<RouteOffsetQuota> {
        val matched = filter(items) { candidate: RouteOffsetQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetQuotas(store: RouteOffsetQuotaStore, query: RouteOffsetQuotaQuery): MutableList<RouteOffsetQuota> {
    return query.apply(store.list())
}
