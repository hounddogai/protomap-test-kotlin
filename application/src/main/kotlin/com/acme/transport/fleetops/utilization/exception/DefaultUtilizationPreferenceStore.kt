package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default utilization preferences by code. */
interface DefaultUtilizationPreferenceStore {
    fun find(code: String): DefaultUtilizationPreference?
    fun save(item: DefaultUtilizationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultUtilizationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultUtilizationPreferenceStore(
    var items: MutableMap<String, DefaultUtilizationPreference>,
    var order: MutableList<String>,
) : DefaultUtilizationPreferenceStore {
    override fun find(code: String): DefaultUtilizationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultUtilizationPreference) {
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

    override fun list(): MutableList<DefaultUtilizationPreference> {
        val out = mutableListOf<DefaultUtilizationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultUtilizationPreferenceStore(): DefaultUtilizationPreferenceStore {
    return MemoryDefaultUtilizationPreferenceStore(
        mutableMapOf<String, DefaultUtilizationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultUtilizationPreferenceStore(
    var inner: DefaultUtilizationPreferenceStore,
    var cache: TtlCache<DefaultUtilizationPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultUtilizationPreferenceStore {
    override fun find(code: String): DefaultUtilizationPreference? {
        val hit = (cache.get(code) as DefaultUtilizationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultUtilizationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultUtilizationPreference> = inner.list()
}

fun newCachedDefaultUtilizationPreferenceStore(inner: DefaultUtilizationPreferenceStore, ttl: Int, clock: Clock): DefaultUtilizationPreferenceStore {
    return CachedDefaultUtilizationPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultUtilizationPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default utilization preferences. Empty fields match all. */
data class DefaultUtilizationPreferenceQuery(
    var source: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultUtilizationPreference): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultUtilizationPreference>): MutableList<DefaultUtilizationPreference> {
        val matched = filter(items) { candidate: DefaultUtilizationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultUtilizationPreferences(store: DefaultUtilizationPreferenceStore, query: DefaultUtilizationPreferenceQuery): MutableList<DefaultUtilizationPreference> {
    return query.apply(store.list())
}
