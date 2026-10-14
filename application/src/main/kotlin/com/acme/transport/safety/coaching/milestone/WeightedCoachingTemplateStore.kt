package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted coaching templates by code. */
interface WeightedCoachingTemplateStore {
    fun find(code: String): WeightedCoachingTemplate?
    fun save(item: WeightedCoachingTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCoachingTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCoachingTemplateStore(
    var items: MutableMap<String, WeightedCoachingTemplate>,
    var order: MutableList<String>,
) : WeightedCoachingTemplateStore {
    override fun find(code: String): WeightedCoachingTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCoachingTemplate) {
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

    override fun list(): MutableList<WeightedCoachingTemplate> {
        val out = mutableListOf<WeightedCoachingTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCoachingTemplateStore(): WeightedCoachingTemplateStore {
    return MemoryWeightedCoachingTemplateStore(
        mutableMapOf<String, WeightedCoachingTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCoachingTemplateStore(
    var inner: WeightedCoachingTemplateStore,
    var cache: TtlCache<WeightedCoachingTemplate>,
    var hits: Int,
    var misses: Int,
) : WeightedCoachingTemplateStore {
    override fun find(code: String): WeightedCoachingTemplate? {
        val hit = (cache.get(code) as WeightedCoachingTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCoachingTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCoachingTemplate> = inner.list()
}

fun newCachedWeightedCoachingTemplateStore(inner: WeightedCoachingTemplateStore, ttl: Int, clock: Clock): WeightedCoachingTemplateStore {
    return CachedWeightedCoachingTemplateStore(
        inner = inner,
        cache = newTtlCache<WeightedCoachingTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted coaching templates. Empty fields match all. */
data class WeightedCoachingTemplateQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCoachingTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCoachingTemplate>): MutableList<WeightedCoachingTemplate> {
        val matched = filter(items) { candidate: WeightedCoachingTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCoachingTemplates(store: WeightedCoachingTemplateStore, query: WeightedCoachingTemplateQuery): MutableList<WeightedCoachingTemplate> {
    return query.apply(store.list())
}
