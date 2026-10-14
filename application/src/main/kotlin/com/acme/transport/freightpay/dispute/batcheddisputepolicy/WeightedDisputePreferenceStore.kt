package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted dispute preferences by code. */
interface WeightedDisputePreferenceStore {
    fun find(code: String): WeightedDisputePreference?
    fun save(item: WeightedDisputePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDisputePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDisputePreferenceStore(
    var items: MutableMap<String, WeightedDisputePreference>,
    var order: MutableList<String>,
) : WeightedDisputePreferenceStore {
    override fun find(code: String): WeightedDisputePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDisputePreference) {
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

    override fun list(): MutableList<WeightedDisputePreference> {
        val out = mutableListOf<WeightedDisputePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDisputePreferenceStore(): WeightedDisputePreferenceStore {
    return MemoryWeightedDisputePreferenceStore(
        mutableMapOf<String, WeightedDisputePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDisputePreferenceStore(
    var inner: WeightedDisputePreferenceStore,
    var cache: TtlCache<WeightedDisputePreference>,
    var hits: Int,
    var misses: Int,
) : WeightedDisputePreferenceStore {
    override fun find(code: String): WeightedDisputePreference? {
        val hit = (cache.get(code) as WeightedDisputePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDisputePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDisputePreference> = inner.list()
}

fun newCachedWeightedDisputePreferenceStore(inner: WeightedDisputePreferenceStore, ttl: Int, clock: Clock): WeightedDisputePreferenceStore {
    return CachedWeightedDisputePreferenceStore(
        inner = inner,
        cache = newTtlCache<WeightedDisputePreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dispute preferences. Empty fields match all. */
data class WeightedDisputePreferenceQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDisputePreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<WeightedDisputePreference>): MutableList<WeightedDisputePreference> {
        val matched = filter(items) { candidate: WeightedDisputePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDisputePreferences(store: WeightedDisputePreferenceStore, query: WeightedDisputePreferenceQuery): MutableList<WeightedDisputePreference> {
    return query.apply(store.list())
}
