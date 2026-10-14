package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted accessorial templates by code. */
interface WeightedAccessorialTemplateStore {
    fun find(code: String): WeightedAccessorialTemplate?
    fun save(item: WeightedAccessorialTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAccessorialTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAccessorialTemplateStore(
    var items: MutableMap<String, WeightedAccessorialTemplate>,
    var order: MutableList<String>,
) : WeightedAccessorialTemplateStore {
    override fun find(code: String): WeightedAccessorialTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAccessorialTemplate) {
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

    override fun list(): MutableList<WeightedAccessorialTemplate> {
        val out = mutableListOf<WeightedAccessorialTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAccessorialTemplateStore(): WeightedAccessorialTemplateStore {
    return MemoryWeightedAccessorialTemplateStore(
        mutableMapOf<String, WeightedAccessorialTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAccessorialTemplateStore(
    var inner: WeightedAccessorialTemplateStore,
    var cache: TtlCache<WeightedAccessorialTemplate>,
    var hits: Int,
    var misses: Int,
) : WeightedAccessorialTemplateStore {
    override fun find(code: String): WeightedAccessorialTemplate? {
        val hit = (cache.get(code) as WeightedAccessorialTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAccessorialTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAccessorialTemplate> = inner.list()
}

fun newCachedWeightedAccessorialTemplateStore(inner: WeightedAccessorialTemplateStore, ttl: Int, clock: Clock): WeightedAccessorialTemplateStore {
    return CachedWeightedAccessorialTemplateStore(
        inner = inner,
        cache = newTtlCache<WeightedAccessorialTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted accessorial templates. Empty fields match all. */
data class WeightedAccessorialTemplateQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAccessorialTemplate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAccessorialTemplate>): MutableList<WeightedAccessorialTemplate> {
        val matched = filter(items) { candidate: WeightedAccessorialTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAccessorialTemplates(store: WeightedAccessorialTemplateStore, query: WeightedAccessorialTemplateQuery): MutableList<WeightedAccessorialTemplate> {
    return query.apply(store.list())
}
