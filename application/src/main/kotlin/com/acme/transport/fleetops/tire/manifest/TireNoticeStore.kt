package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire notices by code. */
interface TireNoticeStore {
    fun find(code: String): TireNotice?
    fun save(item: TireNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireNoticeStore(
    var items: MutableMap<String, TireNotice>,
    var order: MutableList<String>,
) : TireNoticeStore {
    override fun find(code: String): TireNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireNotice) {
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

    override fun list(): MutableList<TireNotice> {
        val out = mutableListOf<TireNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireNoticeStore(): TireNoticeStore {
    return MemoryTireNoticeStore(mutableMapOf<String, TireNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireNoticeStore(
    var inner: TireNoticeStore,
    var cache: TtlCache<TireNotice>,
    var hits: Int,
    var misses: Int,
) : TireNoticeStore {
    override fun find(code: String): TireNotice? {
        val hit = (cache.get(code) as TireNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireNotice> = inner.list()
}

fun newCachedTireNoticeStore(inner: TireNoticeStore, ttl: Int, clock: Clock): TireNoticeStore {
    return CachedTireNoticeStore(
        inner = inner,
        cache = newTtlCache<TireNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire notices. Empty fields match all. */
data class TireNoticeQuery(
    var segment: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireNotice): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireNotice>): MutableList<TireNotice> {
        val matched = filter(items) { candidate: TireNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireNotices(store: TireNoticeStore, query: TireNoticeQuery): MutableList<TireNotice> {
    return query.apply(store.list())
}
