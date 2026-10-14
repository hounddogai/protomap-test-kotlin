package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire entries by code. */
interface TireEntryStore {
    fun find(code: String): TireEntry?
    fun save(item: TireEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireEntryStore(
    var items: MutableMap<String, TireEntry>,
    var order: MutableList<String>,
) : TireEntryStore {
    override fun find(code: String): TireEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireEntry) {
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

    override fun list(): MutableList<TireEntry> {
        val out = mutableListOf<TireEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireEntryStore(): TireEntryStore {
    return MemoryTireEntryStore(mutableMapOf<String, TireEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireEntryStore(
    var inner: TireEntryStore,
    var cache: TtlCache<TireEntry>,
    var hits: Int,
    var misses: Int,
) : TireEntryStore {
    override fun find(code: String): TireEntry? {
        val hit = (cache.get(code) as TireEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireEntry> = inner.list()
}

fun newCachedTireEntryStore(inner: TireEntryStore, ttl: Int, clock: Clock): TireEntryStore {
    return CachedTireEntryStore(
        inner = inner,
        cache = newTtlCache<TireEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire entries. Empty fields match all. */
data class TireEntryQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireEntry>): MutableList<TireEntry> {
        val matched = filter(items) { candidate: TireEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireEntries(store: TireEntryStore, query: TireEntryQuery): MutableList<TireEntry> {
    return query.apply(store.list())
}
