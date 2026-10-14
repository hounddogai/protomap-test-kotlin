package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted tender templates by code. */
interface WeightedTenderTemplateStore {
    fun find(code: String): WeightedTenderTemplate?
    fun save(item: WeightedTenderTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTenderTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTenderTemplateStore(
    var items: MutableMap<String, WeightedTenderTemplate>,
    var order: MutableList<String>,
) : WeightedTenderTemplateStore {
    override fun find(code: String): WeightedTenderTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTenderTemplate) {
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

    override fun list(): MutableList<WeightedTenderTemplate> {
        val out = mutableListOf<WeightedTenderTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTenderTemplateStore(): WeightedTenderTemplateStore {
    return MemoryWeightedTenderTemplateStore(
        mutableMapOf<String, WeightedTenderTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTenderTemplateStore(
    var inner: WeightedTenderTemplateStore,
    var cache: TtlCache<WeightedTenderTemplate>,
    var hits: Int,
    var misses: Int,
) : WeightedTenderTemplateStore {
    override fun find(code: String): WeightedTenderTemplate? {
        val hit = (cache.get(code) as WeightedTenderTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTenderTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTenderTemplate> = inner.list()
}

fun newCachedWeightedTenderTemplateStore(inner: WeightedTenderTemplateStore, ttl: Int, clock: Clock): WeightedTenderTemplateStore {
    return CachedWeightedTenderTemplateStore(
        inner = inner,
        cache = newTtlCache<WeightedTenderTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted tender templates. Empty fields match all. */
data class WeightedTenderTemplateQuery(
    var unit: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTenderTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTenderTemplate>): MutableList<WeightedTenderTemplate> {
        val matched = filter(items) { candidate: WeightedTenderTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTenderTemplates(store: WeightedTenderTemplateStore, query: WeightedTenderTemplateQuery): MutableList<WeightedTenderTemplate> {
    return query.apply(store.list())
}
