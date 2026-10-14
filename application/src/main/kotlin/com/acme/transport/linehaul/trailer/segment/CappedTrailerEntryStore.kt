package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped trailer entries by code. */
interface CappedTrailerEntryStore {
    fun find(code: String): CappedTrailerEntry?
    fun save(item: CappedTrailerEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerEntryStore(
    var items: MutableMap<String, CappedTrailerEntry>,
    var order: MutableList<String>,
) : CappedTrailerEntryStore {
    override fun find(code: String): CappedTrailerEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerEntry) {
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

    override fun list(): MutableList<CappedTrailerEntry> {
        val out = mutableListOf<CappedTrailerEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerEntryStore(): CappedTrailerEntryStore {
    return MemoryCappedTrailerEntryStore(mutableMapOf<String, CappedTrailerEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerEntryStore(
    var inner: CappedTrailerEntryStore,
    var cache: TtlCache<CappedTrailerEntry>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerEntryStore {
    override fun find(code: String): CappedTrailerEntry? {
        val hit = (cache.get(code) as CappedTrailerEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerEntry> = inner.list()
}

fun newCachedCappedTrailerEntryStore(inner: CappedTrailerEntryStore, ttl: Int, clock: Clock): CappedTrailerEntryStore {
    return CachedCappedTrailerEntryStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer entries. Empty fields match all. */
data class CappedTrailerEntryQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTrailerEntry>): MutableList<CappedTrailerEntry> {
        val matched = filter(items) { candidate: CappedTrailerEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerEntries(store: CappedTrailerEntryStore, query: CappedTrailerEntryQuery): MutableList<CappedTrailerEntry> {
    return query.apply(store.list())
}
