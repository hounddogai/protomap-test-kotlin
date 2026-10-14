package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal defect notices by code. */
interface SeasonalDefectNoticeStore {
    fun find(code: String): SeasonalDefectNotice?
    fun save(item: SeasonalDefectNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDefectNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDefectNoticeStore(
    var items: MutableMap<String, SeasonalDefectNotice>,
    var order: MutableList<String>,
) : SeasonalDefectNoticeStore {
    override fun find(code: String): SeasonalDefectNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDefectNotice) {
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

    override fun list(): MutableList<SeasonalDefectNotice> {
        val out = mutableListOf<SeasonalDefectNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDefectNoticeStore(): SeasonalDefectNoticeStore {
    return MemorySeasonalDefectNoticeStore(
        mutableMapOf<String, SeasonalDefectNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDefectNoticeStore(
    var inner: SeasonalDefectNoticeStore,
    var cache: TtlCache<SeasonalDefectNotice>,
    var hits: Int,
    var misses: Int,
) : SeasonalDefectNoticeStore {
    override fun find(code: String): SeasonalDefectNotice? {
        val hit = (cache.get(code) as SeasonalDefectNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDefectNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDefectNotice> = inner.list()
}

fun newCachedSeasonalDefectNoticeStore(inner: SeasonalDefectNoticeStore, ttl: Int, clock: Clock): SeasonalDefectNoticeStore {
    return CachedSeasonalDefectNoticeStore(
        inner = inner,
        cache = newTtlCache<SeasonalDefectNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal defect notices. Empty fields match all. */
data class SeasonalDefectNoticeQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDefectNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDefectNotice>): MutableList<SeasonalDefectNotice> {
        val matched = filter(items) { candidate: SeasonalDefectNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDefectNotices(store: SeasonalDefectNoticeStore, query: SeasonalDefectNoticeQuery): MutableList<SeasonalDefectNotice> {
    return query.apply(store.list())
}
