package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection baselines by code. */
interface WeightedInspectionBaselineStore {
    fun find(code: String): WeightedInspectionBaseline?
    fun save(item: WeightedInspectionBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionBaselineStore(
    var items: MutableMap<String, WeightedInspectionBaseline>,
    var order: MutableList<String>,
) : WeightedInspectionBaselineStore {
    override fun find(code: String): WeightedInspectionBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionBaseline) {
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

    override fun list(): MutableList<WeightedInspectionBaseline> {
        val out = mutableListOf<WeightedInspectionBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionBaselineStore(): WeightedInspectionBaselineStore {
    return MemoryWeightedInspectionBaselineStore(
        mutableMapOf<String, WeightedInspectionBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionBaselineStore(
    var inner: WeightedInspectionBaselineStore,
    var cache: TtlCache<WeightedInspectionBaseline>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionBaselineStore {
    override fun find(code: String): WeightedInspectionBaseline? {
        val hit = (cache.get(code) as WeightedInspectionBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionBaseline> = inner.list()
}

fun newCachedWeightedInspectionBaselineStore(inner: WeightedInspectionBaselineStore, ttl: Int, clock: Clock): WeightedInspectionBaselineStore {
    return CachedWeightedInspectionBaselineStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection baselines. Empty fields match all. */
data class WeightedInspectionBaselineQuery(
    var locale: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionBaseline): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<WeightedInspectionBaseline>): MutableList<WeightedInspectionBaseline> {
        val matched = filter(items) { candidate: WeightedInspectionBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionBaselines(store: WeightedInspectionBaselineStore, query: WeightedInspectionBaselineQuery): MutableList<WeightedInspectionBaseline> {
    return query.apply(store.list())
}
