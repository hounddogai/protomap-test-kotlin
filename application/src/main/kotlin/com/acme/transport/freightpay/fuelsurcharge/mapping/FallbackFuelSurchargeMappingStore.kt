package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback fuel surcharge mappings by code. */
interface FallbackFuelSurchargeMappingStore {
    fun find(code: String): FallbackFuelSurchargeMapping?
    fun save(item: FallbackFuelSurchargeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFuelSurchargeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFuelSurchargeMappingStore(
    var items: MutableMap<String, FallbackFuelSurchargeMapping>,
    var order: MutableList<String>,
) : FallbackFuelSurchargeMappingStore {
    override fun find(code: String): FallbackFuelSurchargeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFuelSurchargeMapping) {
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

    override fun list(): MutableList<FallbackFuelSurchargeMapping> {
        val out = mutableListOf<FallbackFuelSurchargeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFuelSurchargeMappingStore(): FallbackFuelSurchargeMappingStore {
    return MemoryFallbackFuelSurchargeMappingStore(
        mutableMapOf<String, FallbackFuelSurchargeMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFuelSurchargeMappingStore(
    var inner: FallbackFuelSurchargeMappingStore,
    var cache: TtlCache<FallbackFuelSurchargeMapping>,
    var hits: Int,
    var misses: Int,
) : FallbackFuelSurchargeMappingStore {
    override fun find(code: String): FallbackFuelSurchargeMapping? {
        val hit = (cache.get(code) as FallbackFuelSurchargeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFuelSurchargeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFuelSurchargeMapping> = inner.list()
}

fun newCachedFallbackFuelSurchargeMappingStore(inner: FallbackFuelSurchargeMappingStore, ttl: Int, clock: Clock): FallbackFuelSurchargeMappingStore {
    return CachedFallbackFuelSurchargeMappingStore(
        inner = inner,
        cache = newTtlCache<FallbackFuelSurchargeMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback fuel surcharge mappings. Empty fields match all. */
data class FallbackFuelSurchargeMappingQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFuelSurchargeMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackFuelSurchargeMapping>): MutableList<FallbackFuelSurchargeMapping> {
        val matched = filter(items) { candidate: FallbackFuelSurchargeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFuelSurchargeMappings(store: FallbackFuelSurchargeMappingStore, query: FallbackFuelSurchargeMappingQuery): MutableList<FallbackFuelSurchargeMapping> {
    return query.apply(store.list())
}
