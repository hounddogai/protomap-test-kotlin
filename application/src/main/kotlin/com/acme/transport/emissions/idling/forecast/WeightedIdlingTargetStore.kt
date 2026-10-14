package com.acme.transport.emissions.idling.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted idling targets by code. */
interface WeightedIdlingTargetStore {
    fun find(code: String): WeightedIdlingTarget?
    fun save(item: WeightedIdlingTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedIdlingTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedIdlingTargetStore(
    var items: MutableMap<String, WeightedIdlingTarget>,
    var order: MutableList<String>,
) : WeightedIdlingTargetStore {
    override fun find(code: String): WeightedIdlingTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedIdlingTarget) {
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

    override fun list(): MutableList<WeightedIdlingTarget> {
        val out = mutableListOf<WeightedIdlingTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedIdlingTargetStore(): WeightedIdlingTargetStore {
    return MemoryWeightedIdlingTargetStore(
        mutableMapOf<String, WeightedIdlingTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedIdlingTargetStore(
    var inner: WeightedIdlingTargetStore,
    var cache: TtlCache<WeightedIdlingTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedIdlingTargetStore {
    override fun find(code: String): WeightedIdlingTarget? {
        val hit = (cache.get(code) as WeightedIdlingTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedIdlingTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedIdlingTarget> = inner.list()
}

fun newCachedWeightedIdlingTargetStore(inner: WeightedIdlingTargetStore, ttl: Int, clock: Clock): WeightedIdlingTargetStore {
    return CachedWeightedIdlingTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedIdlingTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted idling targets. Empty fields match all. */
data class WeightedIdlingTargetQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedIdlingTarget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedIdlingTarget>): MutableList<WeightedIdlingTarget> {
        val matched = filter(items) { candidate: WeightedIdlingTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedIdlingTargets(store: WeightedIdlingTargetStore, query: WeightedIdlingTargetQuery): MutableList<WeightedIdlingTarget> {
    return query.apply(store.list())
}
