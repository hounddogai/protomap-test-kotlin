package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door notices by code. */
interface DockDoorNoticeStore {
    fun find(code: String): DockDoorNotice?
    fun save(item: DockDoorNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorNoticeStore(
    var items: MutableMap<String, DockDoorNotice>,
    var order: MutableList<String>,
) : DockDoorNoticeStore {
    override fun find(code: String): DockDoorNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorNotice) {
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

    override fun list(): MutableList<DockDoorNotice> {
        val out = mutableListOf<DockDoorNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorNoticeStore(): DockDoorNoticeStore {
    return MemoryDockDoorNoticeStore(mutableMapOf<String, DockDoorNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorNoticeStore(
    var inner: DockDoorNoticeStore,
    var cache: TtlCache<DockDoorNotice>,
    var hits: Int,
    var misses: Int,
) : DockDoorNoticeStore {
    override fun find(code: String): DockDoorNotice? {
        val hit = (cache.get(code) as DockDoorNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorNotice> = inner.list()
}

fun newCachedDockDoorNoticeStore(inner: DockDoorNoticeStore, ttl: Int, clock: Clock): DockDoorNoticeStore {
    return CachedDockDoorNoticeStore(
        inner = inner,
        cache = newTtlCache<DockDoorNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door notices. Empty fields match all. */
data class DockDoorNoticeQuery(
    var locale: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<DockDoorNotice>): MutableList<DockDoorNotice> {
        val matched = filter(items) { candidate: DockDoorNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorNotices(store: DockDoorNoticeStore, query: DockDoorNoticeQuery): MutableList<DockDoorNotice> {
    return query.apply(store.list())
}
