package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary hold notices by code. */
interface PrimaryHoldNoticeStore {
    fun find(code: String): PrimaryHoldNotice?
    fun save(item: PrimaryHoldNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryHoldNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryHoldNoticeStore(
    var items: MutableMap<String, PrimaryHoldNotice>,
    var order: MutableList<String>,
) : PrimaryHoldNoticeStore {
    override fun find(code: String): PrimaryHoldNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryHoldNotice) {
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

    override fun list(): MutableList<PrimaryHoldNotice> {
        val out = mutableListOf<PrimaryHoldNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryHoldNoticeStore(): PrimaryHoldNoticeStore {
    return MemoryPrimaryHoldNoticeStore(mutableMapOf<String, PrimaryHoldNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryHoldNoticeStore(
    var inner: PrimaryHoldNoticeStore,
    var cache: TtlCache<PrimaryHoldNotice>,
    var hits: Int,
    var misses: Int,
) : PrimaryHoldNoticeStore {
    override fun find(code: String): PrimaryHoldNotice? {
        val hit = (cache.get(code) as PrimaryHoldNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryHoldNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryHoldNotice> = inner.list()
}

fun newCachedPrimaryHoldNoticeStore(inner: PrimaryHoldNoticeStore, ttl: Int, clock: Clock): PrimaryHoldNoticeStore {
    return CachedPrimaryHoldNoticeStore(
        inner = inner,
        cache = newTtlCache<PrimaryHoldNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary hold notices. Empty fields match all. */
data class PrimaryHoldNoticeQuery(
    var source: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryHoldNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryHoldNotice>): MutableList<PrimaryHoldNotice> {
        val matched = filter(items) { candidate: PrimaryHoldNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryHoldNotices(store: PrimaryHoldNoticeStore, query: PrimaryHoldNoticeQuery): MutableList<PrimaryHoldNotice> {
    return query.apply(store.list())
}
