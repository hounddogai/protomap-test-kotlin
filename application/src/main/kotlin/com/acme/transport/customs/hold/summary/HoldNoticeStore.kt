package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold notices by code. */
interface HoldNoticeStore {
    fun find(code: String): HoldNotice?
    fun save(item: HoldNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldNoticeStore(
    var items: MutableMap<String, HoldNotice>,
    var order: MutableList<String>,
) : HoldNoticeStore {
    override fun find(code: String): HoldNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldNotice) {
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

    override fun list(): MutableList<HoldNotice> {
        val out = mutableListOf<HoldNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldNoticeStore(): HoldNoticeStore {
    return MemoryHoldNoticeStore(mutableMapOf<String, HoldNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldNoticeStore(
    var inner: HoldNoticeStore,
    var cache: TtlCache<HoldNotice>,
    var hits: Int,
    var misses: Int,
) : HoldNoticeStore {
    override fun find(code: String): HoldNotice? {
        val hit = (cache.get(code) as HoldNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldNotice> = inner.list()
}

fun newCachedHoldNoticeStore(inner: HoldNoticeStore, ttl: Int, clock: Clock): HoldNoticeStore {
    return CachedHoldNoticeStore(
        inner = inner,
        cache = newTtlCache<HoldNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold notices. Empty fields match all. */
data class HoldNoticeQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldNotice): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldNotice>): MutableList<HoldNotice> {
        val matched = filter(items) { candidate: HoldNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldNotices(store: HoldNoticeStore, query: HoldNoticeQuery): MutableList<HoldNotice> {
    return query.apply(store.list())
}
