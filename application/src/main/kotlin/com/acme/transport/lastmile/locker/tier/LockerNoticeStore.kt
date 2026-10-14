package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker notices by code. */
interface LockerNoticeStore {
    fun find(code: String): LockerNotice?
    fun save(item: LockerNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerNoticeStore(
    var items: MutableMap<String, LockerNotice>,
    var order: MutableList<String>,
) : LockerNoticeStore {
    override fun find(code: String): LockerNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerNotice) {
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

    override fun list(): MutableList<LockerNotice> {
        val out = mutableListOf<LockerNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerNoticeStore(): LockerNoticeStore {
    return MemoryLockerNoticeStore(mutableMapOf<String, LockerNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerNoticeStore(
    var inner: LockerNoticeStore,
    var cache: TtlCache<LockerNotice>,
    var hits: Int,
    var misses: Int,
) : LockerNoticeStore {
    override fun find(code: String): LockerNotice? {
        val hit = (cache.get(code) as LockerNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerNotice> = inner.list()
}

fun newCachedLockerNoticeStore(inner: LockerNoticeStore, ttl: Int, clock: Clock): LockerNoticeStore {
    return CachedLockerNoticeStore(
        inner = inner,
        cache = newTtlCache<LockerNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker notices. Empty fields match all. */
data class LockerNoticeQuery(
    var source: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerNotice>): MutableList<LockerNotice> {
        val matched = filter(items) { candidate: LockerNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerNotices(store: LockerNoticeStore, query: LockerNoticeQuery): MutableList<LockerNotice> {
    return query.apply(store.list())
}
