package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan preferences by code. */
interface ServicePlanPreferenceStore {
    fun find(code: String): ServicePlanPreference?
    fun save(item: ServicePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanPreferenceStore(
    var items: MutableMap<String, ServicePlanPreference>,
    var order: MutableList<String>,
) : ServicePlanPreferenceStore {
    override fun find(code: String): ServicePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanPreference) {
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

    override fun list(): MutableList<ServicePlanPreference> {
        val out = mutableListOf<ServicePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanPreferenceStore(): ServicePlanPreferenceStore {
    return MemoryServicePlanPreferenceStore(
        mutableMapOf<String, ServicePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanPreferenceStore(
    var inner: ServicePlanPreferenceStore,
    var cache: TtlCache<ServicePlanPreference>,
    var hits: Int,
    var misses: Int,
) : ServicePlanPreferenceStore {
    override fun find(code: String): ServicePlanPreference? {
        val hit = (cache.get(code) as ServicePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanPreference> = inner.list()
}

fun newCachedServicePlanPreferenceStore(inner: ServicePlanPreferenceStore, ttl: Int, clock: Clock): ServicePlanPreferenceStore {
    return CachedServicePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<ServicePlanPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan preferences. Empty fields match all. */
data class ServicePlanPreferenceQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanPreference>): MutableList<ServicePlanPreference> {
        val matched = filter(items) { candidate: ServicePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanPreferences(store: ServicePlanPreferenceStore, query: ServicePlanPreferenceQuery): MutableList<ServicePlanPreference> {
    return query.apply(store.list())
}
