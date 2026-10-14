package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted scorecard profiles by code. */
interface WeightedScorecardProfileStore {
    fun find(code: String): WeightedScorecardProfile?
    fun save(item: WeightedScorecardProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedScorecardProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedScorecardProfileStore(
    var items: MutableMap<String, WeightedScorecardProfile>,
    var order: MutableList<String>,
) : WeightedScorecardProfileStore {
    override fun find(code: String): WeightedScorecardProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedScorecardProfile) {
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

    override fun list(): MutableList<WeightedScorecardProfile> {
        val out = mutableListOf<WeightedScorecardProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedScorecardProfileStore(): WeightedScorecardProfileStore {
    return MemoryWeightedScorecardProfileStore(
        mutableMapOf<String, WeightedScorecardProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedScorecardProfileStore(
    var inner: WeightedScorecardProfileStore,
    var cache: TtlCache<WeightedScorecardProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedScorecardProfileStore {
    override fun find(code: String): WeightedScorecardProfile? {
        val hit = (cache.get(code) as WeightedScorecardProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedScorecardProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedScorecardProfile> = inner.list()
}

fun newCachedWeightedScorecardProfileStore(inner: WeightedScorecardProfileStore, ttl: Int, clock: Clock): WeightedScorecardProfileStore {
    return CachedWeightedScorecardProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedScorecardProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted scorecard profiles. Empty fields match all. */
data class WeightedScorecardProfileQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedScorecardProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedScorecardProfile>): MutableList<WeightedScorecardProfile> {
        val matched = filter(items) { candidate: WeightedScorecardProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedScorecardProfiles(store: WeightedScorecardProfileStore, query: WeightedScorecardProfileQuery): MutableList<WeightedScorecardProfile> {
    return query.apply(store.list())
}
