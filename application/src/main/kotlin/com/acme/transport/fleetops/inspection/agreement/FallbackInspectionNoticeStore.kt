package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback inspection notices by code. */
interface FallbackInspectionNoticeStore {
    fun find(code: String): FallbackInspectionNotice?
    fun save(item: FallbackInspectionNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackInspectionNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackInspectionNoticeStore(
    var items: MutableMap<String, FallbackInspectionNotice>,
    var order: MutableList<String>,
) : FallbackInspectionNoticeStore {
    override fun find(code: String): FallbackInspectionNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackInspectionNotice) {
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

    override fun list(): MutableList<FallbackInspectionNotice> {
        val out = mutableListOf<FallbackInspectionNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackInspectionNoticeStore(): FallbackInspectionNoticeStore {
    return MemoryFallbackInspectionNoticeStore(
        mutableMapOf<String, FallbackInspectionNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackInspectionNoticeStore(
    var inner: FallbackInspectionNoticeStore,
    var cache: TtlCache<FallbackInspectionNotice>,
    var hits: Int,
    var misses: Int,
) : FallbackInspectionNoticeStore {
    override fun find(code: String): FallbackInspectionNotice? {
        val hit = (cache.get(code) as FallbackInspectionNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackInspectionNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackInspectionNotice> = inner.list()
}

fun newCachedFallbackInspectionNoticeStore(inner: FallbackInspectionNoticeStore, ttl: Int, clock: Clock): FallbackInspectionNoticeStore {
    return CachedFallbackInspectionNoticeStore(
        inner = inner,
        cache = newTtlCache<FallbackInspectionNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback inspection notices. Empty fields match all. */
data class FallbackInspectionNoticeQuery(
    var category: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackInspectionNotice): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackInspectionNotice>): MutableList<FallbackInspectionNotice> {
        val matched = filter(items) { candidate: FallbackInspectionNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackInspectionNotices(store: FallbackInspectionNoticeStore, query: FallbackInspectionNoticeQuery): MutableList<FallbackInspectionNotice> {
    return query.apply(store.list())
}
