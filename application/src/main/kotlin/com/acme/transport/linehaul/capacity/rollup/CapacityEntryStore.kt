package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity entries by code. */
interface CapacityEntryStore {
    fun find(code: String): CapacityEntry?
    fun save(item: CapacityEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityEntryStore(
    var items: MutableMap<String, CapacityEntry>,
    var order: MutableList<String>,
) : CapacityEntryStore {
    override fun find(code: String): CapacityEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityEntry) {
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

    override fun list(): MutableList<CapacityEntry> {
        val out = mutableListOf<CapacityEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityEntryStore(): CapacityEntryStore {
    return MemoryCapacityEntryStore(mutableMapOf<String, CapacityEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityEntryStore(
    var inner: CapacityEntryStore,
    var cache: TtlCache<CapacityEntry>,
    var hits: Int,
    var misses: Int,
) : CapacityEntryStore {
    override fun find(code: String): CapacityEntry? {
        val hit = (cache.get(code) as CapacityEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityEntry> = inner.list()
}

fun newCachedCapacityEntryStore(inner: CapacityEntryStore, ttl: Int, clock: Clock): CapacityEntryStore {
    return CachedCapacityEntryStore(
        inner = inner,
        cache = newTtlCache<CapacityEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity entries. Empty fields match all. */
data class CapacityEntryQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityEntry>): MutableList<CapacityEntry> {
        val matched = filter(items) { candidate: CapacityEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityEntries(store: CapacityEntryStore, query: CapacityEntryQuery): MutableList<CapacityEntry> {
    return query.apply(store.list())
}
