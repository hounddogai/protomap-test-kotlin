package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge mappings by code. */
interface FallbackEvChargeMappingStore {
    fun find(code: String): FallbackEvChargeMapping?
    fun save(item: FallbackEvChargeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeMappingStore(
    var items: MutableMap<String, FallbackEvChargeMapping>,
    var order: MutableList<String>,
) : FallbackEvChargeMappingStore {
    override fun find(code: String): FallbackEvChargeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeMapping) {
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

    override fun list(): MutableList<FallbackEvChargeMapping> {
        val out = mutableListOf<FallbackEvChargeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeMappingStore(): FallbackEvChargeMappingStore {
    return MemoryFallbackEvChargeMappingStore(
        mutableMapOf<String, FallbackEvChargeMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeMappingStore(
    var inner: FallbackEvChargeMappingStore,
    var cache: TtlCache<FallbackEvChargeMapping>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeMappingStore {
    override fun find(code: String): FallbackEvChargeMapping? {
        val hit = (cache.get(code) as FallbackEvChargeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeMapping> = inner.list()
}

fun newCachedFallbackEvChargeMappingStore(inner: FallbackEvChargeMappingStore, ttl: Int, clock: Clock): FallbackEvChargeMappingStore {
    return CachedFallbackEvChargeMappingStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge mappings. Empty fields match all. */
data class FallbackEvChargeMappingQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeMapping): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEvChargeMapping>): MutableList<FallbackEvChargeMapping> {
        val matched = filter(items) { candidate: FallbackEvChargeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeMappings(store: FallbackEvChargeMappingStore, query: FallbackEvChargeMappingQuery): MutableList<FallbackEvChargeMapping> {
    return query.apply(store.list())
}
