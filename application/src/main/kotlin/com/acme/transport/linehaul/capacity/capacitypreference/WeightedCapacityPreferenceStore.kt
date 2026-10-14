package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted capacity preferences by code. */
interface WeightedCapacityPreferenceStore {
    fun find(code: String): WeightedCapacityPreference?
    fun save(item: WeightedCapacityPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCapacityPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCapacityPreferenceStore(
    var items: MutableMap<String, WeightedCapacityPreference>,
    var order: MutableList<String>,
) : WeightedCapacityPreferenceStore {
    override fun find(code: String): WeightedCapacityPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCapacityPreference) {
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

    override fun list(): MutableList<WeightedCapacityPreference> {
        val out = mutableListOf<WeightedCapacityPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCapacityPreferenceStore(): WeightedCapacityPreferenceStore {
    return MemoryWeightedCapacityPreferenceStore(
        mutableMapOf<String, WeightedCapacityPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCapacityPreferenceStore(
    var inner: WeightedCapacityPreferenceStore,
    var cache: TtlCache<WeightedCapacityPreference>,
    var hits: Int,
    var misses: Int,
) : WeightedCapacityPreferenceStore {
    override fun find(code: String): WeightedCapacityPreference? {
        val hit = (cache.get(code) as WeightedCapacityPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCapacityPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCapacityPreference> = inner.list()
}

fun newCachedWeightedCapacityPreferenceStore(inner: WeightedCapacityPreferenceStore, ttl: Int, clock: Clock): WeightedCapacityPreferenceStore {
    return CachedWeightedCapacityPreferenceStore(
        inner = inner,
        cache = newTtlCache<WeightedCapacityPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted capacity preferences. Empty fields match all. */
data class WeightedCapacityPreferenceQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCapacityPreference): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCapacityPreference>): MutableList<WeightedCapacityPreference> {
        val matched = filter(items) { candidate: WeightedCapacityPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCapacityPreferences(store: WeightedCapacityPreferenceStore, query: WeightedCapacityPreferenceQuery): MutableList<WeightedCapacityPreference> {
    return query.apply(store.list())
}
