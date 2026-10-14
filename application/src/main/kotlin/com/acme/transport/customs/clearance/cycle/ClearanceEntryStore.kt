package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance entries by code. */
interface ClearanceEntryStore {
    fun find(code: String): ClearanceEntry?
    fun save(item: ClearanceEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceEntryStore(
    var items: MutableMap<String, ClearanceEntry>,
    var order: MutableList<String>,
) : ClearanceEntryStore {
    override fun find(code: String): ClearanceEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceEntry) {
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

    override fun list(): MutableList<ClearanceEntry> {
        val out = mutableListOf<ClearanceEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceEntryStore(): ClearanceEntryStore {
    return MemoryClearanceEntryStore(mutableMapOf<String, ClearanceEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceEntryStore(
    var inner: ClearanceEntryStore,
    var cache: TtlCache<ClearanceEntry>,
    var hits: Int,
    var misses: Int,
) : ClearanceEntryStore {
    override fun find(code: String): ClearanceEntry? {
        val hit = (cache.get(code) as ClearanceEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceEntry> = inner.list()
}

fun newCachedClearanceEntryStore(inner: ClearanceEntryStore, ttl: Int, clock: Clock): ClearanceEntryStore {
    return CachedClearanceEntryStore(
        inner = inner,
        cache = newTtlCache<ClearanceEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance entries. Empty fields match all. */
data class ClearanceEntryQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceEntry): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ClearanceEntry>): MutableList<ClearanceEntry> {
        val matched = filter(items) { candidate: ClearanceEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceEntries(store: ClearanceEntryStore, query: ClearanceEntryQuery): MutableList<ClearanceEntry> {
    return query.apply(store.list())
}
