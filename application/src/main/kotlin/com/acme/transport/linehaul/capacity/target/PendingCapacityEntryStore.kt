package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending capacity entries by code. */
interface PendingCapacityEntryStore {
    fun find(code: String): PendingCapacityEntry?
    fun save(item: PendingCapacityEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCapacityEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCapacityEntryStore(
    var items: MutableMap<String, PendingCapacityEntry>,
    var order: MutableList<String>,
) : PendingCapacityEntryStore {
    override fun find(code: String): PendingCapacityEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCapacityEntry) {
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

    override fun list(): MutableList<PendingCapacityEntry> {
        val out = mutableListOf<PendingCapacityEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCapacityEntryStore(): PendingCapacityEntryStore {
    return MemoryPendingCapacityEntryStore(
        mutableMapOf<String, PendingCapacityEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCapacityEntryStore(
    var inner: PendingCapacityEntryStore,
    var cache: TtlCache<PendingCapacityEntry>,
    var hits: Int,
    var misses: Int,
) : PendingCapacityEntryStore {
    override fun find(code: String): PendingCapacityEntry? {
        val hit = (cache.get(code) as PendingCapacityEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCapacityEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCapacityEntry> = inner.list()
}

fun newCachedPendingCapacityEntryStore(inner: PendingCapacityEntryStore, ttl: Int, clock: Clock): PendingCapacityEntryStore {
    return CachedPendingCapacityEntryStore(
        inner = inner,
        cache = newTtlCache<PendingCapacityEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending capacity entries. Empty fields match all. */
data class PendingCapacityEntryQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCapacityEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCapacityEntry>): MutableList<PendingCapacityEntry> {
        val matched = filter(items) { candidate: PendingCapacityEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCapacityEntries(store: PendingCapacityEntryStore, query: PendingCapacityEntryQuery): MutableList<PendingCapacityEntry> {
    return query.apply(store.list())
}
