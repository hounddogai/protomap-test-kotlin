package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted cutoff entries by code. */
interface WeightedCutoffEntryStore {
    fun find(code: String): WeightedCutoffEntry?
    fun save(item: WeightedCutoffEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCutoffEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCutoffEntryStore(
    var items: MutableMap<String, WeightedCutoffEntry>,
    var order: MutableList<String>,
) : WeightedCutoffEntryStore {
    override fun find(code: String): WeightedCutoffEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCutoffEntry) {
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

    override fun list(): MutableList<WeightedCutoffEntry> {
        val out = mutableListOf<WeightedCutoffEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCutoffEntryStore(): WeightedCutoffEntryStore {
    return MemoryWeightedCutoffEntryStore(mutableMapOf<String, WeightedCutoffEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCutoffEntryStore(
    var inner: WeightedCutoffEntryStore,
    var cache: TtlCache<WeightedCutoffEntry>,
    var hits: Int,
    var misses: Int,
) : WeightedCutoffEntryStore {
    override fun find(code: String): WeightedCutoffEntry? {
        val hit = (cache.get(code) as WeightedCutoffEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCutoffEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCutoffEntry> = inner.list()
}

fun newCachedWeightedCutoffEntryStore(inner: WeightedCutoffEntryStore, ttl: Int, clock: Clock): WeightedCutoffEntryStore {
    return CachedWeightedCutoffEntryStore(
        inner = inner,
        cache = newTtlCache<WeightedCutoffEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted cutoff entries. Empty fields match all. */
data class WeightedCutoffEntryQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCutoffEntry): Boolean {
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

    fun apply(items: MutableList<WeightedCutoffEntry>): MutableList<WeightedCutoffEntry> {
        val matched = filter(items) { candidate: WeightedCutoffEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCutoffEntries(store: WeightedCutoffEntryStore, query: WeightedCutoffEntryQuery): MutableList<WeightedCutoffEntry> {
    return query.apply(store.list())
}
