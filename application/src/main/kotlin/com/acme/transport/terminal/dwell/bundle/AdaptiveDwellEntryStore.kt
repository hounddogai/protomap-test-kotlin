package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dwell entries by code. */
interface AdaptiveDwellEntryStore {
    fun find(code: String): AdaptiveDwellEntry?
    fun save(item: AdaptiveDwellEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDwellEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDwellEntryStore(
    var items: MutableMap<String, AdaptiveDwellEntry>,
    var order: MutableList<String>,
) : AdaptiveDwellEntryStore {
    override fun find(code: String): AdaptiveDwellEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDwellEntry) {
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

    override fun list(): MutableList<AdaptiveDwellEntry> {
        val out = mutableListOf<AdaptiveDwellEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDwellEntryStore(): AdaptiveDwellEntryStore {
    return MemoryAdaptiveDwellEntryStore(mutableMapOf<String, AdaptiveDwellEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDwellEntryStore(
    var inner: AdaptiveDwellEntryStore,
    var cache: TtlCache<AdaptiveDwellEntry>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDwellEntryStore {
    override fun find(code: String): AdaptiveDwellEntry? {
        val hit = (cache.get(code) as AdaptiveDwellEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDwellEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDwellEntry> = inner.list()
}

fun newCachedAdaptiveDwellEntryStore(inner: AdaptiveDwellEntryStore, ttl: Int, clock: Clock): AdaptiveDwellEntryStore {
    return CachedAdaptiveDwellEntryStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDwellEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dwell entries. Empty fields match all. */
data class AdaptiveDwellEntryQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDwellEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveDwellEntry>): MutableList<AdaptiveDwellEntry> {
        val matched = filter(items) { candidate: AdaptiveDwellEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDwellEntries(store: AdaptiveDwellEntryStore, query: AdaptiveDwellEntryQuery): MutableList<AdaptiveDwellEntry> {
    return query.apply(store.list())
}
