package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance notices by code. */
interface ClearanceNoticeStore {
    fun find(code: String): ClearanceNotice?
    fun save(item: ClearanceNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceNoticeStore(
    var items: MutableMap<String, ClearanceNotice>,
    var order: MutableList<String>,
) : ClearanceNoticeStore {
    override fun find(code: String): ClearanceNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceNotice) {
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

    override fun list(): MutableList<ClearanceNotice> {
        val out = mutableListOf<ClearanceNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceNoticeStore(): ClearanceNoticeStore {
    return MemoryClearanceNoticeStore(mutableMapOf<String, ClearanceNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceNoticeStore(
    var inner: ClearanceNoticeStore,
    var cache: TtlCache<ClearanceNotice>,
    var hits: Int,
    var misses: Int,
) : ClearanceNoticeStore {
    override fun find(code: String): ClearanceNotice? {
        val hit = (cache.get(code) as ClearanceNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceNotice> = inner.list()
}

fun newCachedClearanceNoticeStore(inner: ClearanceNoticeStore, ttl: Int, clock: Clock): ClearanceNoticeStore {
    return CachedClearanceNoticeStore(
        inner = inner,
        cache = newTtlCache<ClearanceNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance notices. Empty fields match all. */
data class ClearanceNoticeQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceNotice>): MutableList<ClearanceNotice> {
        val matched = filter(items) { candidate: ClearanceNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceNotices(store: ClearanceNoticeStore, query: ClearanceNoticeQuery): MutableList<ClearanceNotice> {
    return query.apply(store.list())
}
