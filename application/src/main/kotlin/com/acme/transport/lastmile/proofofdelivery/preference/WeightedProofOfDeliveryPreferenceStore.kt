package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted proof of delivery preferences by code. */
interface WeightedProofOfDeliveryPreferenceStore {
    fun find(code: String): WeightedProofOfDeliveryPreference?
    fun save(item: WeightedProofOfDeliveryPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedProofOfDeliveryPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedProofOfDeliveryPreferenceStore(
    var items: MutableMap<String, WeightedProofOfDeliveryPreference>,
    var order: MutableList<String>,
) : WeightedProofOfDeliveryPreferenceStore {
    override fun find(code: String): WeightedProofOfDeliveryPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedProofOfDeliveryPreference) {
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

    override fun list(): MutableList<WeightedProofOfDeliveryPreference> {
        val out = mutableListOf<WeightedProofOfDeliveryPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedProofOfDeliveryPreferenceStore(): WeightedProofOfDeliveryPreferenceStore {
    return MemoryWeightedProofOfDeliveryPreferenceStore(
        mutableMapOf<String, WeightedProofOfDeliveryPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedProofOfDeliveryPreferenceStore(
    var inner: WeightedProofOfDeliveryPreferenceStore,
    var cache: TtlCache<WeightedProofOfDeliveryPreference>,
    var hits: Int,
    var misses: Int,
) : WeightedProofOfDeliveryPreferenceStore {
    override fun find(code: String): WeightedProofOfDeliveryPreference? {
        val hit = (cache.get(code) as WeightedProofOfDeliveryPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedProofOfDeliveryPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedProofOfDeliveryPreference> = inner.list()
}

fun newCachedWeightedProofOfDeliveryPreferenceStore(inner: WeightedProofOfDeliveryPreferenceStore, ttl: Int, clock: Clock): WeightedProofOfDeliveryPreferenceStore {
    return CachedWeightedProofOfDeliveryPreferenceStore(
        inner = inner,
        cache = newTtlCache<WeightedProofOfDeliveryPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted proof of delivery preferences. Empty fields match all. */
data class WeightedProofOfDeliveryPreferenceQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedProofOfDeliveryPreference): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<WeightedProofOfDeliveryPreference>): MutableList<WeightedProofOfDeliveryPreference> {
        val matched = filter(items) { candidate: WeightedProofOfDeliveryPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedProofOfDeliveryPreferences(store: WeightedProofOfDeliveryPreferenceStore, query: WeightedProofOfDeliveryPreferenceQuery): MutableList<WeightedProofOfDeliveryPreference> {
    return query.apply(store.list())
}
