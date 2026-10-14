package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted service plan templates by code. */
interface WeightedServicePlanTemplateStore {
    fun find(code: String): WeightedServicePlanTemplate?
    fun save(item: WeightedServicePlanTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServicePlanTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServicePlanTemplateStore(
    var items: MutableMap<String, WeightedServicePlanTemplate>,
    var order: MutableList<String>,
) : WeightedServicePlanTemplateStore {
    override fun find(code: String): WeightedServicePlanTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServicePlanTemplate) {
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

    override fun list(): MutableList<WeightedServicePlanTemplate> {
        val out = mutableListOf<WeightedServicePlanTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServicePlanTemplateStore(): WeightedServicePlanTemplateStore {
    return MemoryWeightedServicePlanTemplateStore(
        mutableMapOf<String, WeightedServicePlanTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServicePlanTemplateStore(
    var inner: WeightedServicePlanTemplateStore,
    var cache: TtlCache<WeightedServicePlanTemplate>,
    var hits: Int,
    var misses: Int,
) : WeightedServicePlanTemplateStore {
    override fun find(code: String): WeightedServicePlanTemplate? {
        val hit = (cache.get(code) as WeightedServicePlanTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServicePlanTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServicePlanTemplate> = inner.list()
}

fun newCachedWeightedServicePlanTemplateStore(inner: WeightedServicePlanTemplateStore, ttl: Int, clock: Clock): WeightedServicePlanTemplateStore {
    return CachedWeightedServicePlanTemplateStore(
        inner = inner,
        cache = newTtlCache<WeightedServicePlanTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service plan templates. Empty fields match all. */
data class WeightedServicePlanTemplateQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServicePlanTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedServicePlanTemplate>): MutableList<WeightedServicePlanTemplate> {
        val matched = filter(items) { candidate: WeightedServicePlanTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServicePlanTemplates(store: WeightedServicePlanTemplateStore, query: WeightedServicePlanTemplateQuery): MutableList<WeightedServicePlanTemplate> {
    return query.apply(store.list())
}
