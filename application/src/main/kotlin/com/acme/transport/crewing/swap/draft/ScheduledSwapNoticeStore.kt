package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled swap notices by code. */
interface ScheduledSwapNoticeStore {
    fun find(code: String): ScheduledSwapNotice?
    fun save(item: ScheduledSwapNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSwapNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSwapNoticeStore(
    var items: MutableMap<String, ScheduledSwapNotice>,
    var order: MutableList<String>,
) : ScheduledSwapNoticeStore {
    override fun find(code: String): ScheduledSwapNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSwapNotice) {
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

    override fun list(): MutableList<ScheduledSwapNotice> {
        val out = mutableListOf<ScheduledSwapNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSwapNoticeStore(): ScheduledSwapNoticeStore {
    return MemoryScheduledSwapNoticeStore(mutableMapOf<String, ScheduledSwapNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSwapNoticeStore(
    var inner: ScheduledSwapNoticeStore,
    var cache: TtlCache<ScheduledSwapNotice>,
    var hits: Int,
    var misses: Int,
) : ScheduledSwapNoticeStore {
    override fun find(code: String): ScheduledSwapNotice? {
        val hit = (cache.get(code) as ScheduledSwapNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSwapNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSwapNotice> = inner.list()
}

fun newCachedScheduledSwapNoticeStore(inner: ScheduledSwapNoticeStore, ttl: Int, clock: Clock): ScheduledSwapNoticeStore {
    return CachedScheduledSwapNoticeStore(
        inner = inner,
        cache = newTtlCache<ScheduledSwapNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled swap notices. Empty fields match all. */
data class ScheduledSwapNoticeQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSwapNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledSwapNotice>): MutableList<ScheduledSwapNotice> {
        val matched = filter(items) { candidate: ScheduledSwapNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSwapNotices(store: ScheduledSwapNoticeStore, query: ScheduledSwapNoticeQuery): MutableList<ScheduledSwapNotice> {
    return query.apply(store.list())
}
