package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted emission factor templates by code. */
interface WeightedEmissionFactorTemplateStore {
    fun find(code: String): WeightedEmissionFactorTemplate?
    fun save(item: WeightedEmissionFactorTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEmissionFactorTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEmissionFactorTemplateStore(
    var items: MutableMap<String, WeightedEmissionFactorTemplate>,
    var order: MutableList<String>,
) : WeightedEmissionFactorTemplateStore {
    override fun find(code: String): WeightedEmissionFactorTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEmissionFactorTemplate) {
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

    override fun list(): MutableList<WeightedEmissionFactorTemplate> {
        val out = mutableListOf<WeightedEmissionFactorTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEmissionFactorTemplateStore(): WeightedEmissionFactorTemplateStore {
    return MemoryWeightedEmissionFactorTemplateStore(
        mutableMapOf<String, WeightedEmissionFactorTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEmissionFactorTemplateStore(
    var inner: WeightedEmissionFactorTemplateStore,
    var cache: TtlCache<WeightedEmissionFactorTemplate>,
    var hits: Int,
    var misses: Int,
) : WeightedEmissionFactorTemplateStore {
    override fun find(code: String): WeightedEmissionFactorTemplate? {
        val hit = (cache.get(code) as WeightedEmissionFactorTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEmissionFactorTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEmissionFactorTemplate> = inner.list()
}

fun newCachedWeightedEmissionFactorTemplateStore(inner: WeightedEmissionFactorTemplateStore, ttl: Int, clock: Clock): WeightedEmissionFactorTemplateStore {
    return CachedWeightedEmissionFactorTemplateStore(
        inner = inner,
        cache = newTtlCache<WeightedEmissionFactorTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted emission factor templates. Empty fields match all. */
data class WeightedEmissionFactorTemplateQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEmissionFactorTemplate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<WeightedEmissionFactorTemplate>): MutableList<WeightedEmissionFactorTemplate> {
        val matched = filter(items) { candidate: WeightedEmissionFactorTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEmissionFactorTemplates(store: WeightedEmissionFactorTemplateStore, query: WeightedEmissionFactorTemplateQuery): MutableList<WeightedEmissionFactorTemplate> {
    return query.apply(store.list())
}
