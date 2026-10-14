package com.acme.transport.crewing.availability.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability notices by code. */
interface AvailabilityNoticeStore {
    fun find(code: String): AvailabilityNotice?
    fun save(item: AvailabilityNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityNoticeStore(
    var items: MutableMap<String, AvailabilityNotice>,
    var order: MutableList<String>,
) : AvailabilityNoticeStore {
    override fun find(code: String): AvailabilityNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityNotice) {
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

    override fun list(): MutableList<AvailabilityNotice> {
        val out = mutableListOf<AvailabilityNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityNoticeStore(): AvailabilityNoticeStore {
    return MemoryAvailabilityNoticeStore(mutableMapOf<String, AvailabilityNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityNoticeStore(
    var inner: AvailabilityNoticeStore,
    var cache: TtlCache<AvailabilityNotice>,
    var hits: Int,
    var misses: Int,
) : AvailabilityNoticeStore {
    override fun find(code: String): AvailabilityNotice? {
        val hit = (cache.get(code) as AvailabilityNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityNotice> = inner.list()
}

fun newCachedAvailabilityNoticeStore(inner: AvailabilityNoticeStore, ttl: Int, clock: Clock): AvailabilityNoticeStore {
    return CachedAvailabilityNoticeStore(
        inner = inner,
        cache = newTtlCache<AvailabilityNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability notices. Empty fields match all. */
data class AvailabilityNoticeQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityNotice>): MutableList<AvailabilityNotice> {
        val matched = filter(items) { candidate: AvailabilityNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityNotices(store: AvailabilityNoticeStore, query: AvailabilityNoticeQuery): MutableList<AvailabilityNotice> {
    return query.apply(store.list())
}
