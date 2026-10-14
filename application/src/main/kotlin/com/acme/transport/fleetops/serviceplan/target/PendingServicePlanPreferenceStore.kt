package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending service plan preferences by code. */
interface PendingServicePlanPreferenceStore {
    fun find(code: String): PendingServicePlanPreference?
    fun save(item: PendingServicePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServicePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServicePlanPreferenceStore(
    var items: MutableMap<String, PendingServicePlanPreference>,
    var order: MutableList<String>,
) : PendingServicePlanPreferenceStore {
    override fun find(code: String): PendingServicePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServicePlanPreference) {
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

    override fun list(): MutableList<PendingServicePlanPreference> {
        val out = mutableListOf<PendingServicePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServicePlanPreferenceStore(): PendingServicePlanPreferenceStore {
    return MemoryPendingServicePlanPreferenceStore(
        mutableMapOf<String, PendingServicePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServicePlanPreferenceStore(
    var inner: PendingServicePlanPreferenceStore,
    var cache: TtlCache<PendingServicePlanPreference>,
    var hits: Int,
    var misses: Int,
) : PendingServicePlanPreferenceStore {
    override fun find(code: String): PendingServicePlanPreference? {
        val hit = (cache.get(code) as PendingServicePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServicePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServicePlanPreference> = inner.list()
}

fun newCachedPendingServicePlanPreferenceStore(inner: PendingServicePlanPreferenceStore, ttl: Int, clock: Clock): PendingServicePlanPreferenceStore {
    return CachedPendingServicePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<PendingServicePlanPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service plan preferences. Empty fields match all. */
data class PendingServicePlanPreferenceQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServicePlanPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServicePlanPreference>): MutableList<PendingServicePlanPreference> {
        val matched = filter(items) { candidate: PendingServicePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServicePlanPreferences(store: PendingServicePlanPreferenceStore, query: PendingServicePlanPreferenceQuery): MutableList<PendingServicePlanPreference> {
    return query.apply(store.list())
}
