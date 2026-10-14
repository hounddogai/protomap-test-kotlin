package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard notices by code. */
interface TrailerYardNoticeStore {
    fun find(code: String): TrailerYardNotice?
    fun save(item: TrailerYardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardNoticeStore(
    var items: MutableMap<String, TrailerYardNotice>,
    var order: MutableList<String>,
) : TrailerYardNoticeStore {
    override fun find(code: String): TrailerYardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardNotice) {
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

    override fun list(): MutableList<TrailerYardNotice> {
        val out = mutableListOf<TrailerYardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardNoticeStore(): TrailerYardNoticeStore {
    return MemoryTrailerYardNoticeStore(mutableMapOf<String, TrailerYardNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardNoticeStore(
    var inner: TrailerYardNoticeStore,
    var cache: TtlCache<TrailerYardNotice>,
    var hits: Int,
    var misses: Int,
) : TrailerYardNoticeStore {
    override fun find(code: String): TrailerYardNotice? {
        val hit = (cache.get(code) as TrailerYardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardNotice> = inner.list()
}

fun newCachedTrailerYardNoticeStore(inner: TrailerYardNoticeStore, ttl: Int, clock: Clock): TrailerYardNoticeStore {
    return CachedTrailerYardNoticeStore(
        inner = inner,
        cache = newTtlCache<TrailerYardNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard notices. Empty fields match all. */
data class TrailerYardNoticeQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardNotice): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardNotice>): MutableList<TrailerYardNotice> {
        val matched = filter(items) { candidate: TrailerYardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardNotices(store: TrailerYardNoticeStore, query: TrailerYardNoticeQuery): MutableList<TrailerYardNotice> {
    return query.apply(store.list())
}
