package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled transit time entries by code. */
interface ScheduledTransitTimeEntryStore {
    fun find(code: String): ScheduledTransitTimeEntry?
    fun save(item: ScheduledTransitTimeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTransitTimeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTransitTimeEntryStore(
    var items: MutableMap<String, ScheduledTransitTimeEntry>,
    var order: MutableList<String>,
) : ScheduledTransitTimeEntryStore {
    override fun find(code: String): ScheduledTransitTimeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTransitTimeEntry) {
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

    override fun list(): MutableList<ScheduledTransitTimeEntry> {
        val out = mutableListOf<ScheduledTransitTimeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTransitTimeEntryStore(): ScheduledTransitTimeEntryStore {
    return MemoryScheduledTransitTimeEntryStore(
        mutableMapOf<String, ScheduledTransitTimeEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTransitTimeEntryStore(
    var inner: ScheduledTransitTimeEntryStore,
    var cache: TtlCache<ScheduledTransitTimeEntry>,
    var hits: Int,
    var misses: Int,
) : ScheduledTransitTimeEntryStore {
    override fun find(code: String): ScheduledTransitTimeEntry? {
        val hit = (cache.get(code) as ScheduledTransitTimeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTransitTimeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTransitTimeEntry> = inner.list()
}

fun newCachedScheduledTransitTimeEntryStore(inner: ScheduledTransitTimeEntryStore, ttl: Int, clock: Clock): ScheduledTransitTimeEntryStore {
    return CachedScheduledTransitTimeEntryStore(
        inner = inner,
        cache = newTtlCache<ScheduledTransitTimeEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled transit time entries. Empty fields match all. */
data class ScheduledTransitTimeEntryQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTransitTimeEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledTransitTimeEntry>): MutableList<ScheduledTransitTimeEntry> {
        val matched = filter(items) { candidate: ScheduledTransitTimeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTransitTimeEntries(store: ScheduledTransitTimeEntryStore, query: ScheduledTransitTimeEntryQuery): MutableList<ScheduledTransitTimeEntry> {
    return query.apply(store.list())
}
