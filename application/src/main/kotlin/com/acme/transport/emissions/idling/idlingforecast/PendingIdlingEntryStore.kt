package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending idling entries by code. */
interface PendingIdlingEntryStore {
    fun find(code: String): PendingIdlingEntry?
    fun save(item: PendingIdlingEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingIdlingEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingIdlingEntryStore(
    var items: MutableMap<String, PendingIdlingEntry>,
    var order: MutableList<String>,
) : PendingIdlingEntryStore {
    override fun find(code: String): PendingIdlingEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingIdlingEntry) {
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

    override fun list(): MutableList<PendingIdlingEntry> {
        val out = mutableListOf<PendingIdlingEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingIdlingEntryStore(): PendingIdlingEntryStore {
    return MemoryPendingIdlingEntryStore(mutableMapOf<String, PendingIdlingEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingIdlingEntryStore(
    var inner: PendingIdlingEntryStore,
    var cache: TtlCache<PendingIdlingEntry>,
    var hits: Int,
    var misses: Int,
) : PendingIdlingEntryStore {
    override fun find(code: String): PendingIdlingEntry? {
        val hit = (cache.get(code) as PendingIdlingEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingIdlingEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingIdlingEntry> = inner.list()
}

fun newCachedPendingIdlingEntryStore(inner: PendingIdlingEntryStore, ttl: Int, clock: Clock): PendingIdlingEntryStore {
    return CachedPendingIdlingEntryStore(
        inner = inner,
        cache = newTtlCache<PendingIdlingEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending idling entries. Empty fields match all. */
data class PendingIdlingEntryQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingIdlingEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<PendingIdlingEntry>): MutableList<PendingIdlingEntry> {
        val matched = filter(items) { candidate: PendingIdlingEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingIdlingEntries(store: PendingIdlingEntryStore, query: PendingIdlingEntryQuery): MutableList<PendingIdlingEntry> {
    return query.apply(store.list())
}
