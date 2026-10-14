package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict fuel burn entries by code. */
interface StrictFuelBurnEntryStore {
    fun find(code: String): StrictFuelBurnEntry?
    fun save(item: StrictFuelBurnEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictFuelBurnEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictFuelBurnEntryStore(
    var items: MutableMap<String, StrictFuelBurnEntry>,
    var order: MutableList<String>,
) : StrictFuelBurnEntryStore {
    override fun find(code: String): StrictFuelBurnEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictFuelBurnEntry) {
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

    override fun list(): MutableList<StrictFuelBurnEntry> {
        val out = mutableListOf<StrictFuelBurnEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictFuelBurnEntryStore(): StrictFuelBurnEntryStore {
    return MemoryStrictFuelBurnEntryStore(mutableMapOf<String, StrictFuelBurnEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictFuelBurnEntryStore(
    var inner: StrictFuelBurnEntryStore,
    var cache: TtlCache<StrictFuelBurnEntry>,
    var hits: Int,
    var misses: Int,
) : StrictFuelBurnEntryStore {
    override fun find(code: String): StrictFuelBurnEntry? {
        val hit = (cache.get(code) as StrictFuelBurnEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictFuelBurnEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictFuelBurnEntry> = inner.list()
}

fun newCachedStrictFuelBurnEntryStore(inner: StrictFuelBurnEntryStore, ttl: Int, clock: Clock): StrictFuelBurnEntryStore {
    return CachedStrictFuelBurnEntryStore(
        inner = inner,
        cache = newTtlCache<StrictFuelBurnEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict fuel burn entries. Empty fields match all. */
data class StrictFuelBurnEntryQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictFuelBurnEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictFuelBurnEntry>): MutableList<StrictFuelBurnEntry> {
        val matched = filter(items) { candidate: StrictFuelBurnEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictFuelBurnEntries(store: StrictFuelBurnEntryStore, query: StrictFuelBurnEntryQuery): MutableList<StrictFuelBurnEntry> {
    return query.apply(store.list())
}
