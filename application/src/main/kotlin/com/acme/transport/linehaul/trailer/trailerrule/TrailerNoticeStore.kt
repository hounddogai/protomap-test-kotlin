package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer notices by code. */
interface TrailerNoticeStore {
    fun find(code: String): TrailerNotice?
    fun save(item: TrailerNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerNoticeStore(
    var items: MutableMap<String, TrailerNotice>,
    var order: MutableList<String>,
) : TrailerNoticeStore {
    override fun find(code: String): TrailerNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerNotice) {
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

    override fun list(): MutableList<TrailerNotice> {
        val out = mutableListOf<TrailerNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerNoticeStore(): TrailerNoticeStore {
    return MemoryTrailerNoticeStore(mutableMapOf<String, TrailerNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerNoticeStore(
    var inner: TrailerNoticeStore,
    var cache: TtlCache<TrailerNotice>,
    var hits: Int,
    var misses: Int,
) : TrailerNoticeStore {
    override fun find(code: String): TrailerNotice? {
        val hit = (cache.get(code) as TrailerNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerNotice> = inner.list()
}

fun newCachedTrailerNoticeStore(inner: TrailerNoticeStore, ttl: Int, clock: Clock): TrailerNoticeStore {
    return CachedTrailerNoticeStore(
        inner = inner,
        cache = newTtlCache<TrailerNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer notices. Empty fields match all. */
data class TrailerNoticeQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerNotice>): MutableList<TrailerNotice> {
        val matched = filter(items) { candidate: TrailerNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerNotices(store: TrailerNoticeStore, query: TrailerNoticeQuery): MutableList<TrailerNotice> {
    return query.apply(store.list())
}
