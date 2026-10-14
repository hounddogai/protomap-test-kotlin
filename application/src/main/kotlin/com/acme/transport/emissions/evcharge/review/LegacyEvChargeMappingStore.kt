package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy ev charge mappings by code. */
interface LegacyEvChargeMappingStore {
    fun find(code: String): LegacyEvChargeMapping?
    fun save(item: LegacyEvChargeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyEvChargeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyEvChargeMappingStore(
    var items: MutableMap<String, LegacyEvChargeMapping>,
    var order: MutableList<String>,
) : LegacyEvChargeMappingStore {
    override fun find(code: String): LegacyEvChargeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyEvChargeMapping) {
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

    override fun list(): MutableList<LegacyEvChargeMapping> {
        val out = mutableListOf<LegacyEvChargeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyEvChargeMappingStore(): LegacyEvChargeMappingStore {
    return MemoryLegacyEvChargeMappingStore(
        mutableMapOf<String, LegacyEvChargeMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyEvChargeMappingStore(
    var inner: LegacyEvChargeMappingStore,
    var cache: TtlCache<LegacyEvChargeMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyEvChargeMappingStore {
    override fun find(code: String): LegacyEvChargeMapping? {
        val hit = (cache.get(code) as LegacyEvChargeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyEvChargeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyEvChargeMapping> = inner.list()
}

fun newCachedLegacyEvChargeMappingStore(inner: LegacyEvChargeMappingStore, ttl: Int, clock: Clock): LegacyEvChargeMappingStore {
    return CachedLegacyEvChargeMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyEvChargeMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy ev charge mappings. Empty fields match all. */
data class LegacyEvChargeMappingQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyEvChargeMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyEvChargeMapping>): MutableList<LegacyEvChargeMapping> {
        val matched = filter(items) { candidate: LegacyEvChargeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyEvChargeMappings(store: LegacyEvChargeMappingStore, query: LegacyEvChargeMappingQuery): MutableList<LegacyEvChargeMapping> {
    return query.apply(store.list())
}
