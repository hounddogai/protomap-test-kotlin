package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default inspection notices by code. */
interface DefaultInspectionNoticeStore {
    fun find(code: String): DefaultInspectionNotice?
    fun save(item: DefaultInspectionNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultInspectionNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultInspectionNoticeStore(
    var items: MutableMap<String, DefaultInspectionNotice>,
    var order: MutableList<String>,
) : DefaultInspectionNoticeStore {
    override fun find(code: String): DefaultInspectionNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultInspectionNotice) {
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

    override fun list(): MutableList<DefaultInspectionNotice> {
        val out = mutableListOf<DefaultInspectionNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultInspectionNoticeStore(): DefaultInspectionNoticeStore {
    return MemoryDefaultInspectionNoticeStore(
        mutableMapOf<String, DefaultInspectionNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultInspectionNoticeStore(
    var inner: DefaultInspectionNoticeStore,
    var cache: TtlCache<DefaultInspectionNotice>,
    var hits: Int,
    var misses: Int,
) : DefaultInspectionNoticeStore {
    override fun find(code: String): DefaultInspectionNotice? {
        val hit = (cache.get(code) as DefaultInspectionNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultInspectionNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultInspectionNotice> = inner.list()
}

fun newCachedDefaultInspectionNoticeStore(inner: DefaultInspectionNoticeStore, ttl: Int, clock: Clock): DefaultInspectionNoticeStore {
    return CachedDefaultInspectionNoticeStore(
        inner = inner,
        cache = newTtlCache<DefaultInspectionNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default inspection notices. Empty fields match all. */
data class DefaultInspectionNoticeQuery(
    var segment: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultInspectionNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultInspectionNotice>): MutableList<DefaultInspectionNotice> {
        val matched = filter(items) { candidate: DefaultInspectionNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultInspectionNotices(store: DefaultInspectionNoticeStore, query: DefaultInspectionNoticeQuery): MutableList<DefaultInspectionNotice> {
    return query.apply(store.list())
}
