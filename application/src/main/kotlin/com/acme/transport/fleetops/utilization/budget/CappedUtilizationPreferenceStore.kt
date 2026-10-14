package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped utilization preferences by code. */
interface CappedUtilizationPreferenceStore {
    fun find(code: String): CappedUtilizationPreference?
    fun save(item: CappedUtilizationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedUtilizationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedUtilizationPreferenceStore(
    var items: MutableMap<String, CappedUtilizationPreference>,
    var order: MutableList<String>,
) : CappedUtilizationPreferenceStore {
    override fun find(code: String): CappedUtilizationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedUtilizationPreference) {
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

    override fun list(): MutableList<CappedUtilizationPreference> {
        val out = mutableListOf<CappedUtilizationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedUtilizationPreferenceStore(): CappedUtilizationPreferenceStore {
    return MemoryCappedUtilizationPreferenceStore(
        mutableMapOf<String, CappedUtilizationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedUtilizationPreferenceStore(
    var inner: CappedUtilizationPreferenceStore,
    var cache: TtlCache<CappedUtilizationPreference>,
    var hits: Int,
    var misses: Int,
) : CappedUtilizationPreferenceStore {
    override fun find(code: String): CappedUtilizationPreference? {
        val hit = (cache.get(code) as CappedUtilizationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedUtilizationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedUtilizationPreference> = inner.list()
}

fun newCachedCappedUtilizationPreferenceStore(inner: CappedUtilizationPreferenceStore, ttl: Int, clock: Clock): CappedUtilizationPreferenceStore {
    return CachedCappedUtilizationPreferenceStore(
        inner = inner,
        cache = newTtlCache<CappedUtilizationPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped utilization preferences. Empty fields match all. */
data class CappedUtilizationPreferenceQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedUtilizationPreference): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedUtilizationPreference>): MutableList<CappedUtilizationPreference> {
        val matched = filter(items) { candidate: CappedUtilizationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedUtilizationPreferences(store: CappedUtilizationPreferenceStore, query: CappedUtilizationPreferenceQuery): MutableList<CappedUtilizationPreference> {
    return query.apply(store.list())
}
