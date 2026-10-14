package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted tire scores by code. */
interface WeightedTireScoreStore {
    fun find(code: String): WeightedTireScore?
    fun save(item: WeightedTireScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTireScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTireScoreStore(
    var items: MutableMap<String, WeightedTireScore>,
    var order: MutableList<String>,
) : WeightedTireScoreStore {
    override fun find(code: String): WeightedTireScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTireScore) {
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

    override fun list(): MutableList<WeightedTireScore> {
        val out = mutableListOf<WeightedTireScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTireScoreStore(): WeightedTireScoreStore {
    return MemoryWeightedTireScoreStore(mutableMapOf<String, WeightedTireScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTireScoreStore(
    var inner: WeightedTireScoreStore,
    var cache: TtlCache<WeightedTireScore>,
    var hits: Int,
    var misses: Int,
) : WeightedTireScoreStore {
    override fun find(code: String): WeightedTireScore? {
        val hit = (cache.get(code) as WeightedTireScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTireScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTireScore> = inner.list()
}

fun newCachedWeightedTireScoreStore(inner: WeightedTireScoreStore, ttl: Int, clock: Clock): WeightedTireScoreStore {
    return CachedWeightedTireScoreStore(
        inner = inner,
        cache = newTtlCache<WeightedTireScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted tire scores. Empty fields match all. */
data class WeightedTireScoreQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTireScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTireScore>): MutableList<WeightedTireScore> {
        val matched = filter(items) { candidate: WeightedTireScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTireScores(store: WeightedTireScoreStore, query: WeightedTireScoreQuery): MutableList<WeightedTireScore> {
    return query.apply(store.list())
}
