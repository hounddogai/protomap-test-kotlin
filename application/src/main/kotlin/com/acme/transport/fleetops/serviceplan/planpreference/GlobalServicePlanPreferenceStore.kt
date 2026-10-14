package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global service plan preferences by code. */
interface GlobalServicePlanPreferenceStore {
    fun find(code: String): GlobalServicePlanPreference?
    fun save(item: GlobalServicePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalServicePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalServicePlanPreferenceStore(
    var items: MutableMap<String, GlobalServicePlanPreference>,
    var order: MutableList<String>,
) : GlobalServicePlanPreferenceStore {
    override fun find(code: String): GlobalServicePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalServicePlanPreference) {
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

    override fun list(): MutableList<GlobalServicePlanPreference> {
        val out = mutableListOf<GlobalServicePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalServicePlanPreferenceStore(): GlobalServicePlanPreferenceStore {
    return MemoryGlobalServicePlanPreferenceStore(
        mutableMapOf<String, GlobalServicePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalServicePlanPreferenceStore(
    var inner: GlobalServicePlanPreferenceStore,
    var cache: TtlCache<GlobalServicePlanPreference>,
    var hits: Int,
    var misses: Int,
) : GlobalServicePlanPreferenceStore {
    override fun find(code: String): GlobalServicePlanPreference? {
        val hit = (cache.get(code) as GlobalServicePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalServicePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalServicePlanPreference> = inner.list()
}

fun newCachedGlobalServicePlanPreferenceStore(inner: GlobalServicePlanPreferenceStore, ttl: Int, clock: Clock): GlobalServicePlanPreferenceStore {
    return CachedGlobalServicePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<GlobalServicePlanPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global service plan preferences. Empty fields match all. */
data class GlobalServicePlanPreferenceQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalServicePlanPreference): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<GlobalServicePlanPreference>): MutableList<GlobalServicePlanPreference> {
        val matched = filter(items) { candidate: GlobalServicePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalServicePlanPreferences(store: GlobalServicePlanPreferenceStore, query: GlobalServicePlanPreferenceQuery): MutableList<GlobalServicePlanPreference> {
    return query.apply(store.list())
}
