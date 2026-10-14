package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal route offset notices by code. */
interface SeasonalRouteOffsetNoticeStore {
    fun find(code: String): SeasonalRouteOffsetNotice?
    fun save(item: SeasonalRouteOffsetNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRouteOffsetNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRouteOffsetNoticeStore(
    var items: MutableMap<String, SeasonalRouteOffsetNotice>,
    var order: MutableList<String>,
) : SeasonalRouteOffsetNoticeStore {
    override fun find(code: String): SeasonalRouteOffsetNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRouteOffsetNotice) {
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

    override fun list(): MutableList<SeasonalRouteOffsetNotice> {
        val out = mutableListOf<SeasonalRouteOffsetNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRouteOffsetNoticeStore(): SeasonalRouteOffsetNoticeStore {
    return MemorySeasonalRouteOffsetNoticeStore(
        mutableMapOf<String, SeasonalRouteOffsetNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRouteOffsetNoticeStore(
    var inner: SeasonalRouteOffsetNoticeStore,
    var cache: TtlCache<SeasonalRouteOffsetNotice>,
    var hits: Int,
    var misses: Int,
) : SeasonalRouteOffsetNoticeStore {
    override fun find(code: String): SeasonalRouteOffsetNotice? {
        val hit = (cache.get(code) as SeasonalRouteOffsetNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRouteOffsetNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRouteOffsetNotice> = inner.list()
}

fun newCachedSeasonalRouteOffsetNoticeStore(inner: SeasonalRouteOffsetNoticeStore, ttl: Int, clock: Clock): SeasonalRouteOffsetNoticeStore {
    return CachedSeasonalRouteOffsetNoticeStore(
        inner = inner,
        cache = newTtlCache<SeasonalRouteOffsetNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal route offset notices. Empty fields match all. */
data class SeasonalRouteOffsetNoticeQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRouteOffsetNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRouteOffsetNotice>): MutableList<SeasonalRouteOffsetNotice> {
        val matched = filter(items) { candidate: SeasonalRouteOffsetNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRouteOffsetNotices(store: SeasonalRouteOffsetNoticeStore, query: SeasonalRouteOffsetNoticeQuery): MutableList<SeasonalRouteOffsetNotice> {
    return query.apply(store.list())
}
