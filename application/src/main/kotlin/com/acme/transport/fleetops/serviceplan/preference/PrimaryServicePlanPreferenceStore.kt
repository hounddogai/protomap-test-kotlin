package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary service plan preferences by code. */
interface PrimaryServicePlanPreferenceStore {
    fun find(code: String): PrimaryServicePlanPreference?
    fun save(item: PrimaryServicePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryServicePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryServicePlanPreferenceStore(
    var items: MutableMap<String, PrimaryServicePlanPreference>,
    var order: MutableList<String>,
) : PrimaryServicePlanPreferenceStore {
    override fun find(code: String): PrimaryServicePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryServicePlanPreference) {
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

    override fun list(): MutableList<PrimaryServicePlanPreference> {
        val out = mutableListOf<PrimaryServicePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryServicePlanPreferenceStore(): PrimaryServicePlanPreferenceStore {
    return MemoryPrimaryServicePlanPreferenceStore(
        mutableMapOf<String, PrimaryServicePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryServicePlanPreferenceStore(
    var inner: PrimaryServicePlanPreferenceStore,
    var cache: TtlCache<PrimaryServicePlanPreference>,
    var hits: Int,
    var misses: Int,
) : PrimaryServicePlanPreferenceStore {
    override fun find(code: String): PrimaryServicePlanPreference? {
        val hit = (cache.get(code) as PrimaryServicePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryServicePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryServicePlanPreference> = inner.list()
}

fun newCachedPrimaryServicePlanPreferenceStore(inner: PrimaryServicePlanPreferenceStore, ttl: Int, clock: Clock): PrimaryServicePlanPreferenceStore {
    return CachedPrimaryServicePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<PrimaryServicePlanPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary service plan preferences. Empty fields match all. */
data class PrimaryServicePlanPreferenceQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryServicePlanPreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryServicePlanPreference>): MutableList<PrimaryServicePlanPreference> {
        val matched = filter(items) { candidate: PrimaryServicePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryServicePlanPreferences(store: PrimaryServicePlanPreferenceStore, query: PrimaryServicePlanPreferenceQuery): MutableList<PrimaryServicePlanPreference> {
    return query.apply(store.list())
}
