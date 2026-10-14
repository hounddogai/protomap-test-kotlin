package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted trailer yard entries by code. */
interface WeightedTrailerYardEntryStore {
    fun find(code: String): WeightedTrailerYardEntry?
    fun save(item: WeightedTrailerYardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerYardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerYardEntryStore(
    var items: MutableMap<String, WeightedTrailerYardEntry>,
    var order: MutableList<String>,
) : WeightedTrailerYardEntryStore {
    override fun find(code: String): WeightedTrailerYardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerYardEntry) {
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

    override fun list(): MutableList<WeightedTrailerYardEntry> {
        val out = mutableListOf<WeightedTrailerYardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerYardEntryStore(): WeightedTrailerYardEntryStore {
    return MemoryWeightedTrailerYardEntryStore(
        mutableMapOf<String, WeightedTrailerYardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerYardEntryStore(
    var inner: WeightedTrailerYardEntryStore,
    var cache: TtlCache<WeightedTrailerYardEntry>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerYardEntryStore {
    override fun find(code: String): WeightedTrailerYardEntry? {
        val hit = (cache.get(code) as WeightedTrailerYardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerYardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerYardEntry> = inner.list()
}

fun newCachedWeightedTrailerYardEntryStore(inner: WeightedTrailerYardEntryStore, ttl: Int, clock: Clock): WeightedTrailerYardEntryStore {
    return CachedWeightedTrailerYardEntryStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerYardEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer yard entries. Empty fields match all. */
data class WeightedTrailerYardEntryQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerYardEntry): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerYardEntry>): MutableList<WeightedTrailerYardEntry> {
        val matched = filter(items) { candidate: WeightedTrailerYardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerYardEntries(store: WeightedTrailerYardEntryStore, query: WeightedTrailerYardEntryQuery): MutableList<WeightedTrailerYardEntry> {
    return query.apply(store.list())
}
