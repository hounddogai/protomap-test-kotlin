package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted violation rollups by code. */
interface WeightedViolationRollupStore {
    fun find(code: String): WeightedViolationRollup?
    fun save(item: WeightedViolationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedViolationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedViolationRollupStore(
    var items: MutableMap<String, WeightedViolationRollup>,
    var order: MutableList<String>,
) : WeightedViolationRollupStore {
    override fun find(code: String): WeightedViolationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedViolationRollup) {
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

    override fun list(): MutableList<WeightedViolationRollup> {
        val out = mutableListOf<WeightedViolationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedViolationRollupStore(): WeightedViolationRollupStore {
    return MemoryWeightedViolationRollupStore(
        mutableMapOf<String, WeightedViolationRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedViolationRollupStore(
    var inner: WeightedViolationRollupStore,
    var cache: TtlCache<WeightedViolationRollup>,
    var hits: Int,
    var misses: Int,
) : WeightedViolationRollupStore {
    override fun find(code: String): WeightedViolationRollup? {
        val hit = (cache.get(code) as WeightedViolationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedViolationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedViolationRollup> = inner.list()
}

fun newCachedWeightedViolationRollupStore(inner: WeightedViolationRollupStore, ttl: Int, clock: Clock): WeightedViolationRollupStore {
    return CachedWeightedViolationRollupStore(
        inner = inner,
        cache = newTtlCache<WeightedViolationRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted violation rollups. Empty fields match all. */
data class WeightedViolationRollupQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedViolationRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedViolationRollup>): MutableList<WeightedViolationRollup> {
        val matched = filter(items) { candidate: WeightedViolationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedViolationRollups(store: WeightedViolationRollupStore, query: WeightedViolationRollupQuery): MutableList<WeightedViolationRollup> {
    return query.apply(store.list())
}
