package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset notices by code. */
interface FallbackRouteOffsetNoticeStore {
    fun find(code: String): FallbackRouteOffsetNotice?
    fun save(item: FallbackRouteOffsetNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetNoticeStore(
    var items: MutableMap<String, FallbackRouteOffsetNotice>,
    var order: MutableList<String>,
) : FallbackRouteOffsetNoticeStore {
    override fun find(code: String): FallbackRouteOffsetNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetNotice) {
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

    override fun list(): MutableList<FallbackRouteOffsetNotice> {
        val out = mutableListOf<FallbackRouteOffsetNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetNoticeStore(): FallbackRouteOffsetNoticeStore {
    return MemoryFallbackRouteOffsetNoticeStore(
        mutableMapOf<String, FallbackRouteOffsetNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetNoticeStore(
    var inner: FallbackRouteOffsetNoticeStore,
    var cache: TtlCache<FallbackRouteOffsetNotice>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetNoticeStore {
    override fun find(code: String): FallbackRouteOffsetNotice? {
        val hit = (cache.get(code) as FallbackRouteOffsetNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetNotice> = inner.list()
}

fun newCachedFallbackRouteOffsetNoticeStore(inner: FallbackRouteOffsetNoticeStore, ttl: Int, clock: Clock): FallbackRouteOffsetNoticeStore {
    return CachedFallbackRouteOffsetNoticeStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset notices. Empty fields match all. */
data class FallbackRouteOffsetNoticeQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetNotice): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<FallbackRouteOffsetNotice>): MutableList<FallbackRouteOffsetNotice> {
        val matched = filter(items) { candidate: FallbackRouteOffsetNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetNotices(store: FallbackRouteOffsetNoticeStore, query: FallbackRouteOffsetNoticeQuery): MutableList<FallbackRouteOffsetNotice> {
    return query.apply(store.list())
}
