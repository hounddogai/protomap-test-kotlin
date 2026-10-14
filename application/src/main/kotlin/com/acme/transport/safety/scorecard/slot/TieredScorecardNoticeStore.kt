package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered scorecard notices by code. */
interface TieredScorecardNoticeStore {
    fun find(code: String): TieredScorecardNotice?
    fun save(item: TieredScorecardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredScorecardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredScorecardNoticeStore(
    var items: MutableMap<String, TieredScorecardNotice>,
    var order: MutableList<String>,
) : TieredScorecardNoticeStore {
    override fun find(code: String): TieredScorecardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredScorecardNotice) {
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

    override fun list(): MutableList<TieredScorecardNotice> {
        val out = mutableListOf<TieredScorecardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredScorecardNoticeStore(): TieredScorecardNoticeStore {
    return MemoryTieredScorecardNoticeStore(
        mutableMapOf<String, TieredScorecardNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredScorecardNoticeStore(
    var inner: TieredScorecardNoticeStore,
    var cache: TtlCache<TieredScorecardNotice>,
    var hits: Int,
    var misses: Int,
) : TieredScorecardNoticeStore {
    override fun find(code: String): TieredScorecardNotice? {
        val hit = (cache.get(code) as TieredScorecardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredScorecardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredScorecardNotice> = inner.list()
}

fun newCachedTieredScorecardNoticeStore(inner: TieredScorecardNoticeStore, ttl: Int, clock: Clock): TieredScorecardNoticeStore {
    return CachedTieredScorecardNoticeStore(
        inner = inner,
        cache = newTtlCache<TieredScorecardNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered scorecard notices. Empty fields match all. */
data class TieredScorecardNoticeQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredScorecardNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredScorecardNotice>): MutableList<TieredScorecardNotice> {
        val matched = filter(items) { candidate: TieredScorecardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredScorecardNotices(store: TieredScorecardNoticeStore, query: TieredScorecardNoticeQuery): MutableList<TieredScorecardNotice> {
    return query.apply(store.list())
}
