package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan templates by code. */
interface AdaptiveLanePlanTemplateStore {
    fun find(code: String): AdaptiveLanePlanTemplate?
    fun save(item: AdaptiveLanePlanTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanTemplateStore(
    var items: MutableMap<String, AdaptiveLanePlanTemplate>,
    var order: MutableList<String>,
) : AdaptiveLanePlanTemplateStore {
    override fun find(code: String): AdaptiveLanePlanTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanTemplate) {
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

    override fun list(): MutableList<AdaptiveLanePlanTemplate> {
        val out = mutableListOf<AdaptiveLanePlanTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanTemplateStore(): AdaptiveLanePlanTemplateStore {
    return MemoryAdaptiveLanePlanTemplateStore(
        mutableMapOf<String, AdaptiveLanePlanTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanTemplateStore(
    var inner: AdaptiveLanePlanTemplateStore,
    var cache: TtlCache<AdaptiveLanePlanTemplate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanTemplateStore {
    override fun find(code: String): AdaptiveLanePlanTemplate? {
        val hit = (cache.get(code) as AdaptiveLanePlanTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanTemplate> = inner.list()
}

fun newCachedAdaptiveLanePlanTemplateStore(inner: AdaptiveLanePlanTemplateStore, ttl: Int, clock: Clock): AdaptiveLanePlanTemplateStore {
    return CachedAdaptiveLanePlanTemplateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan templates. Empty fields match all. */
data class AdaptiveLanePlanTemplateQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AdaptiveLanePlanTemplate>): MutableList<AdaptiveLanePlanTemplate> {
        val matched = filter(items) { candidate: AdaptiveLanePlanTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanTemplates(store: AdaptiveLanePlanTemplateStore, query: AdaptiveLanePlanTemplateQuery): MutableList<AdaptiveLanePlanTemplate> {
    return query.apply(store.list())
}
