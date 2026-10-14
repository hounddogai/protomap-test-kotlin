package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery notices by code. */
interface SeasonalRedeliveryNoticeStore {
    fun find(code: String): SeasonalRedeliveryNotice?
    fun save(item: SeasonalRedeliveryNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliveryNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliveryNoticeStore(
    var items: MutableMap<String, SeasonalRedeliveryNotice>,
    var order: MutableList<String>,
) : SeasonalRedeliveryNoticeStore {
    override fun find(code: String): SeasonalRedeliveryNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliveryNotice) {
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

    override fun list(): MutableList<SeasonalRedeliveryNotice> {
        val out = mutableListOf<SeasonalRedeliveryNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliveryNoticeStore(): SeasonalRedeliveryNoticeStore {
    return MemorySeasonalRedeliveryNoticeStore(
        mutableMapOf<String, SeasonalRedeliveryNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliveryNoticeStore(
    var inner: SeasonalRedeliveryNoticeStore,
    var cache: TtlCache<SeasonalRedeliveryNotice>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliveryNoticeStore {
    override fun find(code: String): SeasonalRedeliveryNotice? {
        val hit = (cache.get(code) as SeasonalRedeliveryNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliveryNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliveryNotice> = inner.list()
}

fun newCachedSeasonalRedeliveryNoticeStore(inner: SeasonalRedeliveryNoticeStore, ttl: Int, clock: Clock): SeasonalRedeliveryNoticeStore {
    return CachedSeasonalRedeliveryNoticeStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliveryNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery notices. Empty fields match all. */
data class SeasonalRedeliveryNoticeQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliveryNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRedeliveryNotice>): MutableList<SeasonalRedeliveryNotice> {
        val matched = filter(items) { candidate: SeasonalRedeliveryNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliveryNotices(store: SeasonalRedeliveryNoticeStore, query: SeasonalRedeliveryNoticeQuery): MutableList<SeasonalRedeliveryNotice> {
    return query.apply(store.list())
}
