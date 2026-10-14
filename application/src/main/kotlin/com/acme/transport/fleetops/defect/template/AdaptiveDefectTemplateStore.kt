package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive defect templates by code. */
interface AdaptiveDefectTemplateStore {
    fun find(code: String): AdaptiveDefectTemplate?
    fun save(item: AdaptiveDefectTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDefectTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDefectTemplateStore(
    var items: MutableMap<String, AdaptiveDefectTemplate>,
    var order: MutableList<String>,
) : AdaptiveDefectTemplateStore {
    override fun find(code: String): AdaptiveDefectTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDefectTemplate) {
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

    override fun list(): MutableList<AdaptiveDefectTemplate> {
        val out = mutableListOf<AdaptiveDefectTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDefectTemplateStore(): AdaptiveDefectTemplateStore {
    return MemoryAdaptiveDefectTemplateStore(
        mutableMapOf<String, AdaptiveDefectTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDefectTemplateStore(
    var inner: AdaptiveDefectTemplateStore,
    var cache: TtlCache<AdaptiveDefectTemplate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDefectTemplateStore {
    override fun find(code: String): AdaptiveDefectTemplate? {
        val hit = (cache.get(code) as AdaptiveDefectTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDefectTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDefectTemplate> = inner.list()
}

fun newCachedAdaptiveDefectTemplateStore(inner: AdaptiveDefectTemplateStore, ttl: Int, clock: Clock): AdaptiveDefectTemplateStore {
    return CachedAdaptiveDefectTemplateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDefectTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive defect templates. Empty fields match all. */
data class AdaptiveDefectTemplateQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDefectTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDefectTemplate>): MutableList<AdaptiveDefectTemplate> {
        val matched = filter(items) { candidate: AdaptiveDefectTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDefectTemplates(store: AdaptiveDefectTemplateStore, query: AdaptiveDefectTemplateQuery): MutableList<AdaptiveDefectTemplate> {
    return query.apply(store.list())
}
