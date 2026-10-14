package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled trailer yard entries by code. */
interface ScheduledTrailerYardEntryStore {
    fun find(code: String): ScheduledTrailerYardEntry?
    fun save(item: ScheduledTrailerYardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerYardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerYardEntryStore(
    var items: MutableMap<String, ScheduledTrailerYardEntry>,
    var order: MutableList<String>,
) : ScheduledTrailerYardEntryStore {
    override fun find(code: String): ScheduledTrailerYardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerYardEntry) {
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

    override fun list(): MutableList<ScheduledTrailerYardEntry> {
        val out = mutableListOf<ScheduledTrailerYardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerYardEntryStore(): ScheduledTrailerYardEntryStore {
    return MemoryScheduledTrailerYardEntryStore(
        mutableMapOf<String, ScheduledTrailerYardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerYardEntryStore(
    var inner: ScheduledTrailerYardEntryStore,
    var cache: TtlCache<ScheduledTrailerYardEntry>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerYardEntryStore {
    override fun find(code: String): ScheduledTrailerYardEntry? {
        val hit = (cache.get(code) as ScheduledTrailerYardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerYardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerYardEntry> = inner.list()
}

fun newCachedScheduledTrailerYardEntryStore(inner: ScheduledTrailerYardEntryStore, ttl: Int, clock: Clock): ScheduledTrailerYardEntryStore {
    return CachedScheduledTrailerYardEntryStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerYardEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer yard entries. Empty fields match all. */
data class ScheduledTrailerYardEntryQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerYardEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTrailerYardEntry>): MutableList<ScheduledTrailerYardEntry> {
        val matched = filter(items) { candidate: ScheduledTrailerYardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerYardEntries(store: ScheduledTrailerYardEntryStore, query: ScheduledTrailerYardEntryQuery): MutableList<ScheduledTrailerYardEntry> {
    return query.apply(store.list())
}
