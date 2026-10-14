package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled service area notices by code. */
interface ScheduledServiceAreaNoticeStore {
    fun find(code: String): ScheduledServiceAreaNotice?
    fun save(item: ScheduledServiceAreaNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledServiceAreaNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledServiceAreaNoticeStore(
    var items: MutableMap<String, ScheduledServiceAreaNotice>,
    var order: MutableList<String>,
) : ScheduledServiceAreaNoticeStore {
    override fun find(code: String): ScheduledServiceAreaNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledServiceAreaNotice) {
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

    override fun list(): MutableList<ScheduledServiceAreaNotice> {
        val out = mutableListOf<ScheduledServiceAreaNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledServiceAreaNoticeStore(): ScheduledServiceAreaNoticeStore {
    return MemoryScheduledServiceAreaNoticeStore(
        mutableMapOf<String, ScheduledServiceAreaNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledServiceAreaNoticeStore(
    var inner: ScheduledServiceAreaNoticeStore,
    var cache: TtlCache<ScheduledServiceAreaNotice>,
    var hits: Int,
    var misses: Int,
) : ScheduledServiceAreaNoticeStore {
    override fun find(code: String): ScheduledServiceAreaNotice? {
        val hit = (cache.get(code) as ScheduledServiceAreaNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledServiceAreaNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledServiceAreaNotice> = inner.list()
}

fun newCachedScheduledServiceAreaNoticeStore(inner: ScheduledServiceAreaNoticeStore, ttl: Int, clock: Clock): ScheduledServiceAreaNoticeStore {
    return CachedScheduledServiceAreaNoticeStore(
        inner = inner,
        cache = newTtlCache<ScheduledServiceAreaNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled service area notices. Empty fields match all. */
data class ScheduledServiceAreaNoticeQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledServiceAreaNotice): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledServiceAreaNotice>): MutableList<ScheduledServiceAreaNotice> {
        val matched = filter(items) { candidate: ScheduledServiceAreaNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledServiceAreaNotices(store: ScheduledServiceAreaNoticeStore, query: ScheduledServiceAreaNoticeQuery): MutableList<ScheduledServiceAreaNotice> {
    return query.apply(store.list())
}
