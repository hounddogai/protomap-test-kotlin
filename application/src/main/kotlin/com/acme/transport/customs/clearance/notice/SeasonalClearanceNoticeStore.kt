package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal clearance notices by code. */
interface SeasonalClearanceNoticeStore {
    fun find(code: String): SeasonalClearanceNotice?
    fun save(item: SeasonalClearanceNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalClearanceNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalClearanceNoticeStore(
    var items: MutableMap<String, SeasonalClearanceNotice>,
    var order: MutableList<String>,
) : SeasonalClearanceNoticeStore {
    override fun find(code: String): SeasonalClearanceNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalClearanceNotice) {
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

    override fun list(): MutableList<SeasonalClearanceNotice> {
        val out = mutableListOf<SeasonalClearanceNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalClearanceNoticeStore(): SeasonalClearanceNoticeStore {
    return MemorySeasonalClearanceNoticeStore(
        mutableMapOf<String, SeasonalClearanceNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalClearanceNoticeStore(
    var inner: SeasonalClearanceNoticeStore,
    var cache: TtlCache<SeasonalClearanceNotice>,
    var hits: Int,
    var misses: Int,
) : SeasonalClearanceNoticeStore {
    override fun find(code: String): SeasonalClearanceNotice? {
        val hit = (cache.get(code) as SeasonalClearanceNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalClearanceNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalClearanceNotice> = inner.list()
}

fun newCachedSeasonalClearanceNoticeStore(inner: SeasonalClearanceNoticeStore, ttl: Int, clock: Clock): SeasonalClearanceNoticeStore {
    return CachedSeasonalClearanceNoticeStore(
        inner = inner,
        cache = newTtlCache<SeasonalClearanceNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal clearance notices. Empty fields match all. */
data class SeasonalClearanceNoticeQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalClearanceNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalClearanceNotice>): MutableList<SeasonalClearanceNotice> {
        val matched = filter(items) { candidate: SeasonalClearanceNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalClearanceNotices(store: SeasonalClearanceNoticeStore, query: SeasonalClearanceNoticeQuery): MutableList<SeasonalClearanceNotice> {
    return query.apply(store.list())
}
