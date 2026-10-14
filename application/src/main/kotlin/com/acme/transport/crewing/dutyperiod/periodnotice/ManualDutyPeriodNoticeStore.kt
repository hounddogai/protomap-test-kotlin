package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual duty period notices by code. */
interface ManualDutyPeriodNoticeStore {
    fun find(code: String): ManualDutyPeriodNotice?
    fun save(item: ManualDutyPeriodNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDutyPeriodNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDutyPeriodNoticeStore(
    var items: MutableMap<String, ManualDutyPeriodNotice>,
    var order: MutableList<String>,
) : ManualDutyPeriodNoticeStore {
    override fun find(code: String): ManualDutyPeriodNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDutyPeriodNotice) {
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

    override fun list(): MutableList<ManualDutyPeriodNotice> {
        val out = mutableListOf<ManualDutyPeriodNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDutyPeriodNoticeStore(): ManualDutyPeriodNoticeStore {
    return MemoryManualDutyPeriodNoticeStore(
        mutableMapOf<String, ManualDutyPeriodNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDutyPeriodNoticeStore(
    var inner: ManualDutyPeriodNoticeStore,
    var cache: TtlCache<ManualDutyPeriodNotice>,
    var hits: Int,
    var misses: Int,
) : ManualDutyPeriodNoticeStore {
    override fun find(code: String): ManualDutyPeriodNotice? {
        val hit = (cache.get(code) as ManualDutyPeriodNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDutyPeriodNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDutyPeriodNotice> = inner.list()
}

fun newCachedManualDutyPeriodNoticeStore(inner: ManualDutyPeriodNoticeStore, ttl: Int, clock: Clock): ManualDutyPeriodNoticeStore {
    return CachedManualDutyPeriodNoticeStore(
        inner = inner,
        cache = newTtlCache<ManualDutyPeriodNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual duty period notices. Empty fields match all. */
data class ManualDutyPeriodNoticeQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDutyPeriodNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ManualDutyPeriodNotice>): MutableList<ManualDutyPeriodNotice> {
        val matched = filter(items) { candidate: ManualDutyPeriodNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDutyPeriodNotices(store: ManualDutyPeriodNoticeStore, query: ManualDutyPeriodNoticeQuery): MutableList<ManualDutyPeriodNotice> {
    return query.apply(store.list())
}
