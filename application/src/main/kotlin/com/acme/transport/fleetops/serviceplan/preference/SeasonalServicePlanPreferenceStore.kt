package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal service plan preferences by code. */
interface SeasonalServicePlanPreferenceStore {
    fun find(code: String): SeasonalServicePlanPreference?
    fun save(item: SeasonalServicePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServicePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServicePlanPreferenceStore(
    var items: MutableMap<String, SeasonalServicePlanPreference>,
    var order: MutableList<String>,
) : SeasonalServicePlanPreferenceStore {
    override fun find(code: String): SeasonalServicePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServicePlanPreference) {
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

    override fun list(): MutableList<SeasonalServicePlanPreference> {
        val out = mutableListOf<SeasonalServicePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServicePlanPreferenceStore(): SeasonalServicePlanPreferenceStore {
    return MemorySeasonalServicePlanPreferenceStore(
        mutableMapOf<String, SeasonalServicePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServicePlanPreferenceStore(
    var inner: SeasonalServicePlanPreferenceStore,
    var cache: TtlCache<SeasonalServicePlanPreference>,
    var hits: Int,
    var misses: Int,
) : SeasonalServicePlanPreferenceStore {
    override fun find(code: String): SeasonalServicePlanPreference? {
        val hit = (cache.get(code) as SeasonalServicePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServicePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServicePlanPreference> = inner.list()
}

fun newCachedSeasonalServicePlanPreferenceStore(inner: SeasonalServicePlanPreferenceStore, ttl: Int, clock: Clock): SeasonalServicePlanPreferenceStore {
    return CachedSeasonalServicePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<SeasonalServicePlanPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service plan preferences. Empty fields match all. */
data class SeasonalServicePlanPreferenceQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServicePlanPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServicePlanPreference>): MutableList<SeasonalServicePlanPreference> {
        val matched = filter(items) { candidate: SeasonalServicePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServicePlanPreferences(store: SeasonalServicePlanPreferenceStore, query: SeasonalServicePlanPreferenceQuery): MutableList<SeasonalServicePlanPreference> {
    return query.apply(store.list())
}
