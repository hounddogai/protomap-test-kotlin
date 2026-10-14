package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy swap notices by code. */
interface LegacySwapNoticeStore {
    fun find(code: String): LegacySwapNotice?
    fun save(item: LegacySwapNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySwapNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySwapNoticeStore(
    var items: MutableMap<String, LegacySwapNotice>,
    var order: MutableList<String>,
) : LegacySwapNoticeStore {
    override fun find(code: String): LegacySwapNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySwapNotice) {
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

    override fun list(): MutableList<LegacySwapNotice> {
        val out = mutableListOf<LegacySwapNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySwapNoticeStore(): LegacySwapNoticeStore {
    return MemoryLegacySwapNoticeStore(mutableMapOf<String, LegacySwapNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySwapNoticeStore(
    var inner: LegacySwapNoticeStore,
    var cache: TtlCache<LegacySwapNotice>,
    var hits: Int,
    var misses: Int,
) : LegacySwapNoticeStore {
    override fun find(code: String): LegacySwapNotice? {
        val hit = (cache.get(code) as LegacySwapNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySwapNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySwapNotice> = inner.list()
}

fun newCachedLegacySwapNoticeStore(inner: LegacySwapNoticeStore, ttl: Int, clock: Clock): LegacySwapNoticeStore {
    return CachedLegacySwapNoticeStore(
        inner = inner,
        cache = newTtlCache<LegacySwapNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy swap notices. Empty fields match all. */
data class LegacySwapNoticeQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySwapNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<LegacySwapNotice>): MutableList<LegacySwapNotice> {
        val matched = filter(items) { candidate: LegacySwapNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySwapNotices(store: LegacySwapNoticeStore, query: LegacySwapNoticeQuery): MutableList<LegacySwapNotice> {
    return query.apply(store.list())
}
