package com.acme.transport.crewing.dutyperiod.notice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period notices by code. */
interface DutyPeriodNoticeStore {
    fun find(code: String): DutyPeriodNotice?
    fun save(item: DutyPeriodNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodNoticeStore(
    var items: MutableMap<String, DutyPeriodNotice>,
    var order: MutableList<String>,
) : DutyPeriodNoticeStore {
    override fun find(code: String): DutyPeriodNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodNotice) {
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

    override fun list(): MutableList<DutyPeriodNotice> {
        val out = mutableListOf<DutyPeriodNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodNoticeStore(): DutyPeriodNoticeStore {
    return MemoryDutyPeriodNoticeStore(mutableMapOf<String, DutyPeriodNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodNoticeStore(
    var inner: DutyPeriodNoticeStore,
    var cache: TtlCache<DutyPeriodNotice>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodNoticeStore {
    override fun find(code: String): DutyPeriodNotice? {
        val hit = (cache.get(code) as DutyPeriodNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodNotice> = inner.list()
}

fun newCachedDutyPeriodNoticeStore(inner: DutyPeriodNoticeStore, ttl: Int, clock: Clock): DutyPeriodNoticeStore {
    return CachedDutyPeriodNoticeStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period notices. Empty fields match all. */
data class DutyPeriodNoticeQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DutyPeriodNotice>): MutableList<DutyPeriodNotice> {
        val matched = filter(items) { candidate: DutyPeriodNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodNotices(store: DutyPeriodNoticeStore, query: DutyPeriodNoticeQuery): MutableList<DutyPeriodNotice> {
    return query.apply(store.list())
}
