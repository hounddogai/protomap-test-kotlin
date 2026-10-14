package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard entries by code. */
interface TrailerYardEntryStore {
    fun find(code: String): TrailerYardEntry?
    fun save(item: TrailerYardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardEntryStore(
    var items: MutableMap<String, TrailerYardEntry>,
    var order: MutableList<String>,
) : TrailerYardEntryStore {
    override fun find(code: String): TrailerYardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardEntry) {
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

    override fun list(): MutableList<TrailerYardEntry> {
        val out = mutableListOf<TrailerYardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardEntryStore(): TrailerYardEntryStore {
    return MemoryTrailerYardEntryStore(mutableMapOf<String, TrailerYardEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardEntryStore(
    var inner: TrailerYardEntryStore,
    var cache: TtlCache<TrailerYardEntry>,
    var hits: Int,
    var misses: Int,
) : TrailerYardEntryStore {
    override fun find(code: String): TrailerYardEntry? {
        val hit = (cache.get(code) as TrailerYardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardEntry> = inner.list()
}

fun newCachedTrailerYardEntryStore(inner: TrailerYardEntryStore, ttl: Int, clock: Clock): TrailerYardEntryStore {
    return CachedTrailerYardEntryStore(
        inner = inner,
        cache = newTtlCache<TrailerYardEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard entries. Empty fields match all. */
data class TrailerYardEntryQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardEntry): Boolean {
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

    fun apply(items: MutableList<TrailerYardEntry>): MutableList<TrailerYardEntry> {
        val matched = filter(items) { candidate: TrailerYardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardEntries(store: TrailerYardEntryStore, query: TrailerYardEntryQuery): MutableList<TrailerYardEntry> {
    return query.apply(store.list())
}
