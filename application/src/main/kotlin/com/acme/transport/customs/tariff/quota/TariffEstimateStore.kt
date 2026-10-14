package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff estimates by code. */
interface TariffEstimateStore {
    fun find(code: String): TariffEstimate?
    fun save(item: TariffEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffEstimateStore(
    var items: MutableMap<String, TariffEstimate>,
    var order: MutableList<String>,
) : TariffEstimateStore {
    override fun find(code: String): TariffEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffEstimate) {
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

    override fun list(): MutableList<TariffEstimate> {
        val out = mutableListOf<TariffEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffEstimateStore(): TariffEstimateStore {
    return MemoryTariffEstimateStore(mutableMapOf<String, TariffEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffEstimateStore(
    var inner: TariffEstimateStore,
    var cache: TtlCache<TariffEstimate>,
    var hits: Int,
    var misses: Int,
) : TariffEstimateStore {
    override fun find(code: String): TariffEstimate? {
        val hit = (cache.get(code) as TariffEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffEstimate> = inner.list()
}

fun newCachedTariffEstimateStore(inner: TariffEstimateStore, ttl: Int, clock: Clock): TariffEstimateStore {
    return CachedTariffEstimateStore(
        inner = inner,
        cache = newTtlCache<TariffEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff estimates. Empty fields match all. */
data class TariffEstimateQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<TariffEstimate>): MutableList<TariffEstimate> {
        val matched = filter(items) { candidate: TariffEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffEstimates(store: TariffEstimateStore, query: TariffEstimateQuery): MutableList<TariffEstimate> {
    return query.apply(store.list())
}
