package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending service area entries by code. */
interface PendingServiceAreaEntryStore {
    fun find(code: String): PendingServiceAreaEntry?
    fun save(item: PendingServiceAreaEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServiceAreaEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServiceAreaEntryStore(
    var items: MutableMap<String, PendingServiceAreaEntry>,
    var order: MutableList<String>,
) : PendingServiceAreaEntryStore {
    override fun find(code: String): PendingServiceAreaEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServiceAreaEntry) {
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

    override fun list(): MutableList<PendingServiceAreaEntry> {
        val out = mutableListOf<PendingServiceAreaEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServiceAreaEntryStore(): PendingServiceAreaEntryStore {
    return MemoryPendingServiceAreaEntryStore(
        mutableMapOf<String, PendingServiceAreaEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServiceAreaEntryStore(
    var inner: PendingServiceAreaEntryStore,
    var cache: TtlCache<PendingServiceAreaEntry>,
    var hits: Int,
    var misses: Int,
) : PendingServiceAreaEntryStore {
    override fun find(code: String): PendingServiceAreaEntry? {
        val hit = (cache.get(code) as PendingServiceAreaEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServiceAreaEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServiceAreaEntry> = inner.list()
}

fun newCachedPendingServiceAreaEntryStore(inner: PendingServiceAreaEntryStore, ttl: Int, clock: Clock): PendingServiceAreaEntryStore {
    return CachedPendingServiceAreaEntryStore(
        inner = inner,
        cache = newTtlCache<PendingServiceAreaEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service area entries. Empty fields match all. */
data class PendingServiceAreaEntryQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServiceAreaEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServiceAreaEntry>): MutableList<PendingServiceAreaEntry> {
        val matched = filter(items) { candidate: PendingServiceAreaEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServiceAreaEntries(store: PendingServiceAreaEntryStore, query: PendingServiceAreaEntryQuery): MutableList<PendingServiceAreaEntry> {
    return query.apply(store.list())
}
