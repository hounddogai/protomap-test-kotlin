package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled route offset quotas by code. */
interface ScheduledRouteOffsetQuotaStore {
    fun find(code: String): ScheduledRouteOffsetQuota?
    fun save(item: ScheduledRouteOffsetQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRouteOffsetQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRouteOffsetQuotaStore(
    var items: MutableMap<String, ScheduledRouteOffsetQuota>,
    var order: MutableList<String>,
) : ScheduledRouteOffsetQuotaStore {
    override fun find(code: String): ScheduledRouteOffsetQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRouteOffsetQuota) {
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

    override fun list(): MutableList<ScheduledRouteOffsetQuota> {
        val out = mutableListOf<ScheduledRouteOffsetQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRouteOffsetQuotaStore(): ScheduledRouteOffsetQuotaStore {
    return MemoryScheduledRouteOffsetQuotaStore(
        mutableMapOf<String, ScheduledRouteOffsetQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRouteOffsetQuotaStore(
    var inner: ScheduledRouteOffsetQuotaStore,
    var cache: TtlCache<ScheduledRouteOffsetQuota>,
    var hits: Int,
    var misses: Int,
) : ScheduledRouteOffsetQuotaStore {
    override fun find(code: String): ScheduledRouteOffsetQuota? {
        val hit = (cache.get(code) as ScheduledRouteOffsetQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRouteOffsetQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRouteOffsetQuota> = inner.list()
}

fun newCachedScheduledRouteOffsetQuotaStore(inner: ScheduledRouteOffsetQuotaStore, ttl: Int, clock: Clock): ScheduledRouteOffsetQuotaStore {
    return CachedScheduledRouteOffsetQuotaStore(
        inner = inner,
        cache = newTtlCache<ScheduledRouteOffsetQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled route offset quotas. Empty fields match all. */
data class ScheduledRouteOffsetQuotaQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRouteOffsetQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRouteOffsetQuota>): MutableList<ScheduledRouteOffsetQuota> {
        val matched = filter(items) { candidate: ScheduledRouteOffsetQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRouteOffsetQuotas(store: ScheduledRouteOffsetQuotaStore, query: ScheduledRouteOffsetQuotaQuery): MutableList<ScheduledRouteOffsetQuota> {
    return query.apply(store.list())
}
