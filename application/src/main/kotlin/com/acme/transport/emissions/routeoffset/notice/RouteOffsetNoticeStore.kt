package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset notices by code. */
interface RouteOffsetNoticeStore {
    fun find(code: String): RouteOffsetNotice?
    fun save(item: RouteOffsetNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetNoticeStore(
    var items: MutableMap<String, RouteOffsetNotice>,
    var order: MutableList<String>,
) : RouteOffsetNoticeStore {
    override fun find(code: String): RouteOffsetNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetNotice) {
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

    override fun list(): MutableList<RouteOffsetNotice> {
        val out = mutableListOf<RouteOffsetNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetNoticeStore(): RouteOffsetNoticeStore {
    return MemoryRouteOffsetNoticeStore(mutableMapOf<String, RouteOffsetNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetNoticeStore(
    var inner: RouteOffsetNoticeStore,
    var cache: TtlCache<RouteOffsetNotice>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetNoticeStore {
    override fun find(code: String): RouteOffsetNotice? {
        val hit = (cache.get(code) as RouteOffsetNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetNotice> = inner.list()
}

fun newCachedRouteOffsetNoticeStore(inner: RouteOffsetNoticeStore, ttl: Int, clock: Clock): RouteOffsetNoticeStore {
    return CachedRouteOffsetNoticeStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset notices. Empty fields match all. */
data class RouteOffsetNoticeQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetNotice>): MutableList<RouteOffsetNotice> {
        val matched = filter(items) { candidate: RouteOffsetNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetNotices(store: RouteOffsetNoticeStore, query: RouteOffsetNoticeQuery): MutableList<RouteOffsetNotice> {
    return query.apply(store.list())
}
