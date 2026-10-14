package com.acme.transport.customs.inspection.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection notices by code. */
interface PendingInspectionNoticeStore {
    fun find(code: String): PendingInspectionNotice?
    fun save(item: PendingInspectionNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionNoticeStore(
    var items: MutableMap<String, PendingInspectionNotice>,
    var order: MutableList<String>,
) : PendingInspectionNoticeStore {
    override fun find(code: String): PendingInspectionNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionNotice) {
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

    override fun list(): MutableList<PendingInspectionNotice> {
        val out = mutableListOf<PendingInspectionNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionNoticeStore(): PendingInspectionNoticeStore {
    return MemoryPendingInspectionNoticeStore(
        mutableMapOf<String, PendingInspectionNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionNoticeStore(
    var inner: PendingInspectionNoticeStore,
    var cache: TtlCache<PendingInspectionNotice>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionNoticeStore {
    override fun find(code: String): PendingInspectionNotice? {
        val hit = (cache.get(code) as PendingInspectionNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionNotice> = inner.list()
}

fun newCachedPendingInspectionNoticeStore(inner: PendingInspectionNoticeStore, ttl: Int, clock: Clock): PendingInspectionNoticeStore {
    return CachedPendingInspectionNoticeStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection notices. Empty fields match all. */
data class PendingInspectionNoticeQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionNotice>): MutableList<PendingInspectionNotice> {
        val matched = filter(items) { candidate: PendingInspectionNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionNotices(store: PendingInspectionNoticeStore, query: PendingInspectionNoticeQuery): MutableList<PendingInspectionNotice> {
    return query.apply(store.list())
}
