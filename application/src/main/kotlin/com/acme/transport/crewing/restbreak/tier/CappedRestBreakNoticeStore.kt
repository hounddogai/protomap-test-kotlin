package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped rest break notices by code. */
interface CappedRestBreakNoticeStore {
    fun find(code: String): CappedRestBreakNotice?
    fun save(item: CappedRestBreakNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRestBreakNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRestBreakNoticeStore(
    var items: MutableMap<String, CappedRestBreakNotice>,
    var order: MutableList<String>,
) : CappedRestBreakNoticeStore {
    override fun find(code: String): CappedRestBreakNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRestBreakNotice) {
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

    override fun list(): MutableList<CappedRestBreakNotice> {
        val out = mutableListOf<CappedRestBreakNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRestBreakNoticeStore(): CappedRestBreakNoticeStore {
    return MemoryCappedRestBreakNoticeStore(
        mutableMapOf<String, CappedRestBreakNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRestBreakNoticeStore(
    var inner: CappedRestBreakNoticeStore,
    var cache: TtlCache<CappedRestBreakNotice>,
    var hits: Int,
    var misses: Int,
) : CappedRestBreakNoticeStore {
    override fun find(code: String): CappedRestBreakNotice? {
        val hit = (cache.get(code) as CappedRestBreakNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRestBreakNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRestBreakNotice> = inner.list()
}

fun newCachedCappedRestBreakNoticeStore(inner: CappedRestBreakNoticeStore, ttl: Int, clock: Clock): CappedRestBreakNoticeStore {
    return CachedCappedRestBreakNoticeStore(
        inner = inner,
        cache = newTtlCache<CappedRestBreakNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped rest break notices. Empty fields match all. */
data class CappedRestBreakNoticeQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRestBreakNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRestBreakNotice>): MutableList<CappedRestBreakNotice> {
        val matched = filter(items) { candidate: CappedRestBreakNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRestBreakNotices(store: CappedRestBreakNoticeStore, query: CappedRestBreakNoticeQuery): MutableList<CappedRestBreakNotice> {
    return query.apply(store.list())
}
