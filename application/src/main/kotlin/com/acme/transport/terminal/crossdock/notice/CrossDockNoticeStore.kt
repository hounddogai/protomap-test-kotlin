package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock notices by code. */
interface CrossDockNoticeStore {
    fun find(code: String): CrossDockNotice?
    fun save(item: CrossDockNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockNoticeStore(
    var items: MutableMap<String, CrossDockNotice>,
    var order: MutableList<String>,
) : CrossDockNoticeStore {
    override fun find(code: String): CrossDockNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockNotice) {
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

    override fun list(): MutableList<CrossDockNotice> {
        val out = mutableListOf<CrossDockNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockNoticeStore(): CrossDockNoticeStore {
    return MemoryCrossDockNoticeStore(mutableMapOf<String, CrossDockNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockNoticeStore(
    var inner: CrossDockNoticeStore,
    var cache: TtlCache<CrossDockNotice>,
    var hits: Int,
    var misses: Int,
) : CrossDockNoticeStore {
    override fun find(code: String): CrossDockNotice? {
        val hit = (cache.get(code) as CrossDockNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockNotice> = inner.list()
}

fun newCachedCrossDockNoticeStore(inner: CrossDockNoticeStore, ttl: Int, clock: Clock): CrossDockNoticeStore {
    return CachedCrossDockNoticeStore(
        inner = inner,
        cache = newTtlCache<CrossDockNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock notices. Empty fields match all. */
data class CrossDockNoticeQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CrossDockNotice>): MutableList<CrossDockNotice> {
        val matched = filter(items) { candidate: CrossDockNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockNotices(store: CrossDockNoticeStore, query: CrossDockNoticeQuery): MutableList<CrossDockNotice> {
    return query.apply(store.list())
}
