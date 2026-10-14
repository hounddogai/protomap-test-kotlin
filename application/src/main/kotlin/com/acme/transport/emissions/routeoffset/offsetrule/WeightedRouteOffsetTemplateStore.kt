package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted route offset templates by code. */
interface WeightedRouteOffsetTemplateStore {
    fun find(code: String): WeightedRouteOffsetTemplate?
    fun save(item: WeightedRouteOffsetTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRouteOffsetTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRouteOffsetTemplateStore(
    var items: MutableMap<String, WeightedRouteOffsetTemplate>,
    var order: MutableList<String>,
) : WeightedRouteOffsetTemplateStore {
    override fun find(code: String): WeightedRouteOffsetTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRouteOffsetTemplate) {
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

    override fun list(): MutableList<WeightedRouteOffsetTemplate> {
        val out = mutableListOf<WeightedRouteOffsetTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRouteOffsetTemplateStore(): WeightedRouteOffsetTemplateStore {
    return MemoryWeightedRouteOffsetTemplateStore(
        mutableMapOf<String, WeightedRouteOffsetTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRouteOffsetTemplateStore(
    var inner: WeightedRouteOffsetTemplateStore,
    var cache: TtlCache<WeightedRouteOffsetTemplate>,
    var hits: Int,
    var misses: Int,
) : WeightedRouteOffsetTemplateStore {
    override fun find(code: String): WeightedRouteOffsetTemplate? {
        val hit = (cache.get(code) as WeightedRouteOffsetTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRouteOffsetTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRouteOffsetTemplate> = inner.list()
}

fun newCachedWeightedRouteOffsetTemplateStore(inner: WeightedRouteOffsetTemplateStore, ttl: Int, clock: Clock): WeightedRouteOffsetTemplateStore {
    return CachedWeightedRouteOffsetTemplateStore(
        inner = inner,
        cache = newTtlCache<WeightedRouteOffsetTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted route offset templates. Empty fields match all. */
data class WeightedRouteOffsetTemplateQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRouteOffsetTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRouteOffsetTemplate>): MutableList<WeightedRouteOffsetTemplate> {
        val matched = filter(items) { candidate: WeightedRouteOffsetTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRouteOffsetTemplates(store: WeightedRouteOffsetTemplateStore, query: WeightedRouteOffsetTemplateQuery): MutableList<WeightedRouteOffsetTemplate> {
    return query.apply(store.list())
}
