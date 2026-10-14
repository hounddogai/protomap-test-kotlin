package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door notices by code. */
interface ScheduledDockDoorNoticeStore {
    fun find(code: String): ScheduledDockDoorNotice?
    fun save(item: ScheduledDockDoorNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorNoticeStore(
    var items: MutableMap<String, ScheduledDockDoorNotice>,
    var order: MutableList<String>,
) : ScheduledDockDoorNoticeStore {
    override fun find(code: String): ScheduledDockDoorNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorNotice) {
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

    override fun list(): MutableList<ScheduledDockDoorNotice> {
        val out = mutableListOf<ScheduledDockDoorNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorNoticeStore(): ScheduledDockDoorNoticeStore {
    return MemoryScheduledDockDoorNoticeStore(
        mutableMapOf<String, ScheduledDockDoorNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorNoticeStore(
    var inner: ScheduledDockDoorNoticeStore,
    var cache: TtlCache<ScheduledDockDoorNotice>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorNoticeStore {
    override fun find(code: String): ScheduledDockDoorNotice? {
        val hit = (cache.get(code) as ScheduledDockDoorNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorNotice> = inner.list()
}

fun newCachedScheduledDockDoorNoticeStore(inner: ScheduledDockDoorNoticeStore, ttl: Int, clock: Clock): ScheduledDockDoorNoticeStore {
    return CachedScheduledDockDoorNoticeStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door notices. Empty fields match all. */
data class ScheduledDockDoorNoticeQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDockDoorNotice>): MutableList<ScheduledDockDoorNotice> {
        val matched = filter(items) { candidate: ScheduledDockDoorNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorNotices(store: ScheduledDockDoorNoticeStore, query: ScheduledDockDoorNoticeQuery): MutableList<ScheduledDockDoorNotice> {
    return query.apply(store.list())
}
