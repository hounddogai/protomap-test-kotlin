package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dwell notices by code. */
interface WeightedDwellNoticeStore {
    fun find(code: String): WeightedDwellNotice?
    fun save(item: WeightedDwellNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDwellNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDwellNoticeStore(
    var items: MutableMap<String, WeightedDwellNotice>,
    var order: MutableList<String>,
) : WeightedDwellNoticeStore {
    override fun find(code: String): WeightedDwellNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDwellNotice) {
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

    override fun list(): MutableList<WeightedDwellNotice> {
        val out = mutableListOf<WeightedDwellNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDwellNoticeStore(): WeightedDwellNoticeStore {
    return MemoryWeightedDwellNoticeStore(mutableMapOf<String, WeightedDwellNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDwellNoticeStore(
    var inner: WeightedDwellNoticeStore,
    var cache: TtlCache<WeightedDwellNotice>,
    var hits: Int,
    var misses: Int,
) : WeightedDwellNoticeStore {
    override fun find(code: String): WeightedDwellNotice? {
        val hit = (cache.get(code) as WeightedDwellNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDwellNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDwellNotice> = inner.list()
}

fun newCachedWeightedDwellNoticeStore(inner: WeightedDwellNoticeStore, ttl: Int, clock: Clock): WeightedDwellNoticeStore {
    return CachedWeightedDwellNoticeStore(
        inner = inner,
        cache = newTtlCache<WeightedDwellNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dwell notices. Empty fields match all. */
data class WeightedDwellNoticeQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDwellNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDwellNotice>): MutableList<WeightedDwellNotice> {
        val matched = filter(items) { candidate: WeightedDwellNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDwellNotices(store: WeightedDwellNoticeStore, query: WeightedDwellNoticeQuery): MutableList<WeightedDwellNotice> {
    return query.apply(store.list())
}
