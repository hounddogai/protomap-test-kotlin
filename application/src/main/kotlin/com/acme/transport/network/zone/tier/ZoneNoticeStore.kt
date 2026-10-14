package com.acme.transport.network.zone.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone notices by code. */
interface ZoneNoticeStore {
    fun find(code: String): ZoneNotice?
    fun save(item: ZoneNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneNoticeStore(
    var items: MutableMap<String, ZoneNotice>,
    var order: MutableList<String>,
) : ZoneNoticeStore {
    override fun find(code: String): ZoneNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneNotice) {
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

    override fun list(): MutableList<ZoneNotice> {
        val out = mutableListOf<ZoneNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneNoticeStore(): ZoneNoticeStore {
    return MemoryZoneNoticeStore(mutableMapOf<String, ZoneNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneNoticeStore(
    var inner: ZoneNoticeStore,
    var cache: TtlCache<ZoneNotice>,
    var hits: Int,
    var misses: Int,
) : ZoneNoticeStore {
    override fun find(code: String): ZoneNotice? {
        val hit = (cache.get(code) as ZoneNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneNotice> = inner.list()
}

fun newCachedZoneNoticeStore(inner: ZoneNoticeStore, ttl: Int, clock: Clock): ZoneNoticeStore {
    return CachedZoneNoticeStore(
        inner = inner,
        cache = newTtlCache<ZoneNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone notices. Empty fields match all. */
data class ZoneNoticeQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneNotice): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneNotice>): MutableList<ZoneNotice> {
        val matched = filter(items) { candidate: ZoneNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneNotices(store: ZoneNoticeStore, query: ZoneNoticeQuery): MutableList<ZoneNotice> {
    return query.apply(store.list())
}
