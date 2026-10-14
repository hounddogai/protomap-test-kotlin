package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted duty period targets by code. */
interface WeightedDutyPeriodTargetStore {
    fun find(code: String): WeightedDutyPeriodTarget?
    fun save(item: WeightedDutyPeriodTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDutyPeriodTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDutyPeriodTargetStore(
    var items: MutableMap<String, WeightedDutyPeriodTarget>,
    var order: MutableList<String>,
) : WeightedDutyPeriodTargetStore {
    override fun find(code: String): WeightedDutyPeriodTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDutyPeriodTarget) {
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

    override fun list(): MutableList<WeightedDutyPeriodTarget> {
        val out = mutableListOf<WeightedDutyPeriodTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDutyPeriodTargetStore(): WeightedDutyPeriodTargetStore {
    return MemoryWeightedDutyPeriodTargetStore(
        mutableMapOf<String, WeightedDutyPeriodTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDutyPeriodTargetStore(
    var inner: WeightedDutyPeriodTargetStore,
    var cache: TtlCache<WeightedDutyPeriodTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedDutyPeriodTargetStore {
    override fun find(code: String): WeightedDutyPeriodTarget? {
        val hit = (cache.get(code) as WeightedDutyPeriodTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDutyPeriodTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDutyPeriodTarget> = inner.list()
}

fun newCachedWeightedDutyPeriodTargetStore(inner: WeightedDutyPeriodTargetStore, ttl: Int, clock: Clock): WeightedDutyPeriodTargetStore {
    return CachedWeightedDutyPeriodTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedDutyPeriodTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted duty period targets. Empty fields match all. */
data class WeightedDutyPeriodTargetQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDutyPeriodTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDutyPeriodTarget>): MutableList<WeightedDutyPeriodTarget> {
        val matched = filter(items) { candidate: WeightedDutyPeriodTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDutyPeriodTargets(store: WeightedDutyPeriodTargetStore, query: WeightedDutyPeriodTargetQuery): MutableList<WeightedDutyPeriodTarget> {
    return query.apply(store.list())
}
