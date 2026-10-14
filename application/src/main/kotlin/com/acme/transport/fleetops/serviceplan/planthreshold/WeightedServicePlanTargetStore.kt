package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted service plan targets by code. */
interface WeightedServicePlanTargetStore {
    fun find(code: String): WeightedServicePlanTarget?
    fun save(item: WeightedServicePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServicePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServicePlanTargetStore(
    var items: MutableMap<String, WeightedServicePlanTarget>,
    var order: MutableList<String>,
) : WeightedServicePlanTargetStore {
    override fun find(code: String): WeightedServicePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServicePlanTarget) {
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

    override fun list(): MutableList<WeightedServicePlanTarget> {
        val out = mutableListOf<WeightedServicePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServicePlanTargetStore(): WeightedServicePlanTargetStore {
    return MemoryWeightedServicePlanTargetStore(
        mutableMapOf<String, WeightedServicePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServicePlanTargetStore(
    var inner: WeightedServicePlanTargetStore,
    var cache: TtlCache<WeightedServicePlanTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedServicePlanTargetStore {
    override fun find(code: String): WeightedServicePlanTarget? {
        val hit = (cache.get(code) as WeightedServicePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServicePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServicePlanTarget> = inner.list()
}

fun newCachedWeightedServicePlanTargetStore(inner: WeightedServicePlanTargetStore, ttl: Int, clock: Clock): WeightedServicePlanTargetStore {
    return CachedWeightedServicePlanTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedServicePlanTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service plan targets. Empty fields match all. */
data class WeightedServicePlanTargetQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServicePlanTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedServicePlanTarget>): MutableList<WeightedServicePlanTarget> {
        val matched = filter(items) { candidate: WeightedServicePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServicePlanTargets(store: WeightedServicePlanTargetStore, query: WeightedServicePlanTargetQuery): MutableList<WeightedServicePlanTarget> {
    return query.apply(store.list())
}
