package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect notices by code. */
interface DefectNoticeStore {
    fun find(code: String): DefectNotice?
    fun save(item: DefectNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectNoticeStore(
    var items: MutableMap<String, DefectNotice>,
    var order: MutableList<String>,
) : DefectNoticeStore {
    override fun find(code: String): DefectNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectNotice) {
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

    override fun list(): MutableList<DefectNotice> {
        val out = mutableListOf<DefectNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectNoticeStore(): DefectNoticeStore {
    return MemoryDefectNoticeStore(mutableMapOf<String, DefectNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectNoticeStore(
    var inner: DefectNoticeStore,
    var cache: TtlCache<DefectNotice>,
    var hits: Int,
    var misses: Int,
) : DefectNoticeStore {
    override fun find(code: String): DefectNotice? {
        val hit = (cache.get(code) as DefectNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectNotice> = inner.list()
}

fun newCachedDefectNoticeStore(inner: DefectNoticeStore, ttl: Int, clock: Clock): DefectNoticeStore {
    return CachedDefectNoticeStore(
        inner = inner,
        cache = newTtlCache<DefectNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect notices. Empty fields match all. */
data class DefectNoticeQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectNotice>): MutableList<DefectNotice> {
        val matched = filter(items) { candidate: DefectNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectNotices(store: DefectNoticeStore, query: DefectNoticeQuery): MutableList<DefectNotice> {
    return query.apply(store.list())
}
