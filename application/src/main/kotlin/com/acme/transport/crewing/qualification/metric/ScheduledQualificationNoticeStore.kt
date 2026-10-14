package com.acme.transport.crewing.qualification.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled qualification notices by code. */
interface ScheduledQualificationNoticeStore {
    fun find(code: String): ScheduledQualificationNotice?
    fun save(item: ScheduledQualificationNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledQualificationNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledQualificationNoticeStore(
    var items: MutableMap<String, ScheduledQualificationNotice>,
    var order: MutableList<String>,
) : ScheduledQualificationNoticeStore {
    override fun find(code: String): ScheduledQualificationNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledQualificationNotice) {
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

    override fun list(): MutableList<ScheduledQualificationNotice> {
        val out = mutableListOf<ScheduledQualificationNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledQualificationNoticeStore(): ScheduledQualificationNoticeStore {
    return MemoryScheduledQualificationNoticeStore(
        mutableMapOf<String, ScheduledQualificationNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledQualificationNoticeStore(
    var inner: ScheduledQualificationNoticeStore,
    var cache: TtlCache<ScheduledQualificationNotice>,
    var hits: Int,
    var misses: Int,
) : ScheduledQualificationNoticeStore {
    override fun find(code: String): ScheduledQualificationNotice? {
        val hit = (cache.get(code) as ScheduledQualificationNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledQualificationNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledQualificationNotice> = inner.list()
}

fun newCachedScheduledQualificationNoticeStore(inner: ScheduledQualificationNoticeStore, ttl: Int, clock: Clock): ScheduledQualificationNoticeStore {
    return CachedScheduledQualificationNoticeStore(
        inner = inner,
        cache = newTtlCache<ScheduledQualificationNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled qualification notices. Empty fields match all. */
data class ScheduledQualificationNoticeQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledQualificationNotice): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledQualificationNotice>): MutableList<ScheduledQualificationNotice> {
        val matched = filter(items) { candidate: ScheduledQualificationNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledQualificationNotices(store: ScheduledQualificationNoticeStore, query: ScheduledQualificationNoticeQuery): MutableList<ScheduledQualificationNotice> {
    return query.apply(store.list())
}
