package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal utilization preferences by code. */
interface SeasonalUtilizationPreferenceStore {
    fun find(code: String): SeasonalUtilizationPreference?
    fun save(item: SeasonalUtilizationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalUtilizationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalUtilizationPreferenceStore(
    var items: MutableMap<String, SeasonalUtilizationPreference>,
    var order: MutableList<String>,
) : SeasonalUtilizationPreferenceStore {
    override fun find(code: String): SeasonalUtilizationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalUtilizationPreference) {
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

    override fun list(): MutableList<SeasonalUtilizationPreference> {
        val out = mutableListOf<SeasonalUtilizationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalUtilizationPreferenceStore(): SeasonalUtilizationPreferenceStore {
    return MemorySeasonalUtilizationPreferenceStore(
        mutableMapOf<String, SeasonalUtilizationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalUtilizationPreferenceStore(
    var inner: SeasonalUtilizationPreferenceStore,
    var cache: TtlCache<SeasonalUtilizationPreference>,
    var hits: Int,
    var misses: Int,
) : SeasonalUtilizationPreferenceStore {
    override fun find(code: String): SeasonalUtilizationPreference? {
        val hit = (cache.get(code) as SeasonalUtilizationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalUtilizationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalUtilizationPreference> = inner.list()
}

fun newCachedSeasonalUtilizationPreferenceStore(inner: SeasonalUtilizationPreferenceStore, ttl: Int, clock: Clock): SeasonalUtilizationPreferenceStore {
    return CachedSeasonalUtilizationPreferenceStore(
        inner = inner,
        cache = newTtlCache<SeasonalUtilizationPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal utilization preferences. Empty fields match all. */
data class SeasonalUtilizationPreferenceQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalUtilizationPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SeasonalUtilizationPreference>): MutableList<SeasonalUtilizationPreference> {
        val matched = filter(items) { candidate: SeasonalUtilizationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalUtilizationPreferences(store: SeasonalUtilizationPreferenceStore, query: SeasonalUtilizationPreferenceQuery): MutableList<SeasonalUtilizationPreference> {
    return query.apply(store.list())
}
