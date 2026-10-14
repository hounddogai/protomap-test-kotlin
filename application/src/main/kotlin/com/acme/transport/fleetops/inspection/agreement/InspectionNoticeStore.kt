package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection notices by code. */
interface InspectionNoticeStore {
    fun find(code: String): InspectionNotice?
    fun save(item: InspectionNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionNoticeStore(
    var items: MutableMap<String, InspectionNotice>,
    var order: MutableList<String>,
) : InspectionNoticeStore {
    override fun find(code: String): InspectionNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionNotice) {
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

    override fun list(): MutableList<InspectionNotice> {
        val out = mutableListOf<InspectionNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionNoticeStore(): InspectionNoticeStore {
    return MemoryInspectionNoticeStore(mutableMapOf<String, InspectionNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionNoticeStore(
    var inner: InspectionNoticeStore,
    var cache: TtlCache<InspectionNotice>,
    var hits: Int,
    var misses: Int,
) : InspectionNoticeStore {
    override fun find(code: String): InspectionNotice? {
        val hit = (cache.get(code) as InspectionNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionNotice> = inner.list()
}

fun newCachedInspectionNoticeStore(inner: InspectionNoticeStore, ttl: Int, clock: Clock): InspectionNoticeStore {
    return CachedInspectionNoticeStore(
        inner = inner,
        cache = newTtlCache<InspectionNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection notices. Empty fields match all. */
data class InspectionNoticeQuery(
    var unit: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionNotice): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionNotice>): MutableList<InspectionNotice> {
        val matched = filter(items) { candidate: InspectionNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionNotices(store: InspectionNoticeStore, query: InspectionNoticeQuery): MutableList<InspectionNotice> {
    return query.apply(store.list())
}
