package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted audit mappings by code. */
interface WeightedAuditMappingStore {
    fun find(code: String): WeightedAuditMapping?
    fun save(item: WeightedAuditMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAuditMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAuditMappingStore(
    var items: MutableMap<String, WeightedAuditMapping>,
    var order: MutableList<String>,
) : WeightedAuditMappingStore {
    override fun find(code: String): WeightedAuditMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAuditMapping) {
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

    override fun list(): MutableList<WeightedAuditMapping> {
        val out = mutableListOf<WeightedAuditMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAuditMappingStore(): WeightedAuditMappingStore {
    return MemoryWeightedAuditMappingStore(
        mutableMapOf<String, WeightedAuditMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAuditMappingStore(
    var inner: WeightedAuditMappingStore,
    var cache: TtlCache<WeightedAuditMapping>,
    var hits: Int,
    var misses: Int,
) : WeightedAuditMappingStore {
    override fun find(code: String): WeightedAuditMapping? {
        val hit = (cache.get(code) as WeightedAuditMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAuditMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAuditMapping> = inner.list()
}

fun newCachedWeightedAuditMappingStore(inner: WeightedAuditMappingStore, ttl: Int, clock: Clock): WeightedAuditMappingStore {
    return CachedWeightedAuditMappingStore(
        inner = inner,
        cache = newTtlCache<WeightedAuditMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted audit mappings. Empty fields match all. */
data class WeightedAuditMappingQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAuditMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAuditMapping>): MutableList<WeightedAuditMapping> {
        val matched = filter(items) { candidate: WeightedAuditMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAuditMappings(store: WeightedAuditMappingStore, query: WeightedAuditMappingQuery): MutableList<WeightedAuditMapping> {
    return query.apply(store.list())
}
