package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback tariff estimates by code. */
interface FallbackTariffEstimateStore {
    fun find(code: String): FallbackTariffEstimate?
    fun save(item: FallbackTariffEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTariffEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTariffEstimateStore(
    var items: MutableMap<String, FallbackTariffEstimate>,
    var order: MutableList<String>,
) : FallbackTariffEstimateStore {
    override fun find(code: String): FallbackTariffEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTariffEstimate) {
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

    override fun list(): MutableList<FallbackTariffEstimate> {
        val out = mutableListOf<FallbackTariffEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTariffEstimateStore(): FallbackTariffEstimateStore {
    return MemoryFallbackTariffEstimateStore(
        mutableMapOf<String, FallbackTariffEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTariffEstimateStore(
    var inner: FallbackTariffEstimateStore,
    var cache: TtlCache<FallbackTariffEstimate>,
    var hits: Int,
    var misses: Int,
) : FallbackTariffEstimateStore {
    override fun find(code: String): FallbackTariffEstimate? {
        val hit = (cache.get(code) as FallbackTariffEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTariffEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTariffEstimate> = inner.list()
}

fun newCachedFallbackTariffEstimateStore(inner: FallbackTariffEstimateStore, ttl: Int, clock: Clock): FallbackTariffEstimateStore {
    return CachedFallbackTariffEstimateStore(
        inner = inner,
        cache = newTtlCache<FallbackTariffEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tariff estimates. Empty fields match all. */
data class FallbackTariffEstimateQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTariffEstimate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<FallbackTariffEstimate>): MutableList<FallbackTariffEstimate> {
        val matched = filter(items) { candidate: FallbackTariffEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTariffEstimates(store: FallbackTariffEstimateStore, query: FallbackTariffEstimateQuery): MutableList<FallbackTariffEstimate> {
    return query.apply(store.list())
}
