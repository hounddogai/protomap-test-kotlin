package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rest break preferences by code. */
interface WeightedRestBreakPreferenceStore {
    fun find(code: String): WeightedRestBreakPreference?
    fun save(item: WeightedRestBreakPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRestBreakPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRestBreakPreferenceStore(
    var items: MutableMap<String, WeightedRestBreakPreference>,
    var order: MutableList<String>,
) : WeightedRestBreakPreferenceStore {
    override fun find(code: String): WeightedRestBreakPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRestBreakPreference) {
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

    override fun list(): MutableList<WeightedRestBreakPreference> {
        val out = mutableListOf<WeightedRestBreakPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRestBreakPreferenceStore(): WeightedRestBreakPreferenceStore {
    return MemoryWeightedRestBreakPreferenceStore(
        mutableMapOf<String, WeightedRestBreakPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRestBreakPreferenceStore(
    var inner: WeightedRestBreakPreferenceStore,
    var cache: TtlCache<WeightedRestBreakPreference>,
    var hits: Int,
    var misses: Int,
) : WeightedRestBreakPreferenceStore {
    override fun find(code: String): WeightedRestBreakPreference? {
        val hit = (cache.get(code) as WeightedRestBreakPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRestBreakPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRestBreakPreference> = inner.list()
}

fun newCachedWeightedRestBreakPreferenceStore(inner: WeightedRestBreakPreferenceStore, ttl: Int, clock: Clock): WeightedRestBreakPreferenceStore {
    return CachedWeightedRestBreakPreferenceStore(
        inner = inner,
        cache = newTtlCache<WeightedRestBreakPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rest break preferences. Empty fields match all. */
data class WeightedRestBreakPreferenceQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRestBreakPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRestBreakPreference>): MutableList<WeightedRestBreakPreference> {
        val matched = filter(items) { candidate: WeightedRestBreakPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRestBreakPreferences(store: WeightedRestBreakPreferenceStore, query: WeightedRestBreakPreferenceQuery): MutableList<WeightedRestBreakPreference> {
    return query.apply(store.list())
}
