package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan preferences by code. */
interface ManualServicePlanPreferenceStore {
    fun find(code: String): ManualServicePlanPreference?
    fun save(item: ManualServicePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanPreferenceStore(
    var items: MutableMap<String, ManualServicePlanPreference>,
    var order: MutableList<String>,
) : ManualServicePlanPreferenceStore {
    override fun find(code: String): ManualServicePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanPreference) {
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

    override fun list(): MutableList<ManualServicePlanPreference> {
        val out = mutableListOf<ManualServicePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanPreferenceStore(): ManualServicePlanPreferenceStore {
    return MemoryManualServicePlanPreferenceStore(
        mutableMapOf<String, ManualServicePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanPreferenceStore(
    var inner: ManualServicePlanPreferenceStore,
    var cache: TtlCache<ManualServicePlanPreference>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanPreferenceStore {
    override fun find(code: String): ManualServicePlanPreference? {
        val hit = (cache.get(code) as ManualServicePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanPreference> = inner.list()
}

fun newCachedManualServicePlanPreferenceStore(inner: ManualServicePlanPreferenceStore, ttl: Int, clock: Clock): ManualServicePlanPreferenceStore {
    return CachedManualServicePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan preferences. Empty fields match all. */
data class ManualServicePlanPreferenceQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanPreference): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanPreference>): MutableList<ManualServicePlanPreference> {
        val matched = filter(items) { candidate: ManualServicePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanPreferences(store: ManualServicePlanPreferenceStore, query: ManualServicePlanPreferenceQuery): MutableList<ManualServicePlanPreference> {
    return query.apply(store.list())
}
