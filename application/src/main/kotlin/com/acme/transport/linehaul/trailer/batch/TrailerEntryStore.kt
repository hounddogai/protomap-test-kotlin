package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer entries by code. */
interface TrailerEntryStore {
    fun find(code: String): TrailerEntry?
    fun save(item: TrailerEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerEntryStore(
    var items: MutableMap<String, TrailerEntry>,
    var order: MutableList<String>,
) : TrailerEntryStore {
    override fun find(code: String): TrailerEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerEntry) {
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

    override fun list(): MutableList<TrailerEntry> {
        val out = mutableListOf<TrailerEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerEntryStore(): TrailerEntryStore {
    return MemoryTrailerEntryStore(mutableMapOf<String, TrailerEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerEntryStore(
    var inner: TrailerEntryStore,
    var cache: TtlCache<TrailerEntry>,
    var hits: Int,
    var misses: Int,
) : TrailerEntryStore {
    override fun find(code: String): TrailerEntry? {
        val hit = (cache.get(code) as TrailerEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerEntry> = inner.list()
}

fun newCachedTrailerEntryStore(inner: TrailerEntryStore, ttl: Int, clock: Clock): TrailerEntryStore {
    return CachedTrailerEntryStore(
        inner = inner,
        cache = newTtlCache<TrailerEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer entries. Empty fields match all. */
data class TrailerEntryQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerEntry): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerEntry>): MutableList<TrailerEntry> {
        val matched = filter(items) { candidate: TrailerEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerEntries(store: TrailerEntryStore, query: TrailerEntryQuery): MutableList<TrailerEntry> {
    return query.apply(store.list())
}
