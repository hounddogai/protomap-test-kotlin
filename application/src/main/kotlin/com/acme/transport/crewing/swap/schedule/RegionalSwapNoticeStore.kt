package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional swap notices by code. */
interface RegionalSwapNoticeStore {
    fun find(code: String): RegionalSwapNotice?
    fun save(item: RegionalSwapNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalSwapNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalSwapNoticeStore(
    var items: MutableMap<String, RegionalSwapNotice>,
    var order: MutableList<String>,
) : RegionalSwapNoticeStore {
    override fun find(code: String): RegionalSwapNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalSwapNotice) {
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

    override fun list(): MutableList<RegionalSwapNotice> {
        val out = mutableListOf<RegionalSwapNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalSwapNoticeStore(): RegionalSwapNoticeStore {
    return MemoryRegionalSwapNoticeStore(mutableMapOf<String, RegionalSwapNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalSwapNoticeStore(
    var inner: RegionalSwapNoticeStore,
    var cache: TtlCache<RegionalSwapNotice>,
    var hits: Int,
    var misses: Int,
) : RegionalSwapNoticeStore {
    override fun find(code: String): RegionalSwapNotice? {
        val hit = (cache.get(code) as RegionalSwapNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalSwapNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalSwapNotice> = inner.list()
}

fun newCachedRegionalSwapNoticeStore(inner: RegionalSwapNoticeStore, ttl: Int, clock: Clock): RegionalSwapNoticeStore {
    return CachedRegionalSwapNoticeStore(
        inner = inner,
        cache = newTtlCache<RegionalSwapNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional swap notices. Empty fields match all. */
data class RegionalSwapNoticeQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalSwapNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalSwapNotice>): MutableList<RegionalSwapNotice> {
        val matched = filter(items) { candidate: RegionalSwapNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalSwapNotices(store: RegionalSwapNoticeStore, query: RegionalSwapNoticeQuery): MutableList<RegionalSwapNotice> {
    return query.apply(store.list())
}
