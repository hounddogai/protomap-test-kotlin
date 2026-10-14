package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell notices by code. */
interface DwellNoticeStore {
    fun find(code: String): DwellNotice?
    fun save(item: DwellNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellNoticeStore(
    var items: MutableMap<String, DwellNotice>,
    var order: MutableList<String>,
) : DwellNoticeStore {
    override fun find(code: String): DwellNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellNotice) {
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

    override fun list(): MutableList<DwellNotice> {
        val out = mutableListOf<DwellNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellNoticeStore(): DwellNoticeStore {
    return MemoryDwellNoticeStore(mutableMapOf<String, DwellNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellNoticeStore(
    var inner: DwellNoticeStore,
    var cache: TtlCache<DwellNotice>,
    var hits: Int,
    var misses: Int,
) : DwellNoticeStore {
    override fun find(code: String): DwellNotice? {
        val hit = (cache.get(code) as DwellNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellNotice> = inner.list()
}

fun newCachedDwellNoticeStore(inner: DwellNoticeStore, ttl: Int, clock: Clock): DwellNoticeStore {
    return CachedDwellNoticeStore(
        inner = inner,
        cache = newTtlCache<DwellNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell notices. Empty fields match all. */
data class DwellNoticeQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellNotice): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellNotice>): MutableList<DwellNotice> {
        val matched = filter(items) { candidate: DwellNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellNotices(store: DwellNoticeStore, query: DwellNoticeQuery): MutableList<DwellNotice> {
    return query.apply(store.list())
}
