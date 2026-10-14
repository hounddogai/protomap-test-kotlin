package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive cross dock notices by code. */
interface AdaptiveCrossDockNoticeStore {
    fun find(code: String): AdaptiveCrossDockNotice?
    fun save(item: AdaptiveCrossDockNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCrossDockNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCrossDockNoticeStore(
    var items: MutableMap<String, AdaptiveCrossDockNotice>,
    var order: MutableList<String>,
) : AdaptiveCrossDockNoticeStore {
    override fun find(code: String): AdaptiveCrossDockNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCrossDockNotice) {
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

    override fun list(): MutableList<AdaptiveCrossDockNotice> {
        val out = mutableListOf<AdaptiveCrossDockNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCrossDockNoticeStore(): AdaptiveCrossDockNoticeStore {
    return MemoryAdaptiveCrossDockNoticeStore(
        mutableMapOf<String, AdaptiveCrossDockNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCrossDockNoticeStore(
    var inner: AdaptiveCrossDockNoticeStore,
    var cache: TtlCache<AdaptiveCrossDockNotice>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCrossDockNoticeStore {
    override fun find(code: String): AdaptiveCrossDockNotice? {
        val hit = (cache.get(code) as AdaptiveCrossDockNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCrossDockNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCrossDockNotice> = inner.list()
}

fun newCachedAdaptiveCrossDockNoticeStore(inner: AdaptiveCrossDockNoticeStore, ttl: Int, clock: Clock): AdaptiveCrossDockNoticeStore {
    return CachedAdaptiveCrossDockNoticeStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCrossDockNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive cross dock notices. Empty fields match all. */
data class AdaptiveCrossDockNoticeQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCrossDockNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCrossDockNotice>): MutableList<AdaptiveCrossDockNotice> {
        val matched = filter(items) { candidate: AdaptiveCrossDockNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCrossDockNotices(store: AdaptiveCrossDockNoticeStore, query: AdaptiveCrossDockNoticeQuery): MutableList<AdaptiveCrossDockNotice> {
    return query.apply(store.list())
}
