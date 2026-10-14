package com.acme.transport.customs.tariff.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tariff estimates by code. */
interface DefaultTariffEstimateStore {
    fun find(code: String): DefaultTariffEstimate?
    fun save(item: DefaultTariffEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTariffEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTariffEstimateStore(
    var items: MutableMap<String, DefaultTariffEstimate>,
    var order: MutableList<String>,
) : DefaultTariffEstimateStore {
    override fun find(code: String): DefaultTariffEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTariffEstimate) {
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

    override fun list(): MutableList<DefaultTariffEstimate> {
        val out = mutableListOf<DefaultTariffEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTariffEstimateStore(): DefaultTariffEstimateStore {
    return MemoryDefaultTariffEstimateStore(
        mutableMapOf<String, DefaultTariffEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTariffEstimateStore(
    var inner: DefaultTariffEstimateStore,
    var cache: TtlCache<DefaultTariffEstimate>,
    var hits: Int,
    var misses: Int,
) : DefaultTariffEstimateStore {
    override fun find(code: String): DefaultTariffEstimate? {
        val hit = (cache.get(code) as DefaultTariffEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTariffEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTariffEstimate> = inner.list()
}

fun newCachedDefaultTariffEstimateStore(inner: DefaultTariffEstimateStore, ttl: Int, clock: Clock): DefaultTariffEstimateStore {
    return CachedDefaultTariffEstimateStore(
        inner = inner,
        cache = newTtlCache<DefaultTariffEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tariff estimates. Empty fields match all. */
data class DefaultTariffEstimateQuery(
    var lane: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTariffEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTariffEstimate>): MutableList<DefaultTariffEstimate> {
        val matched = filter(items) { candidate: DefaultTariffEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTariffEstimates(store: DefaultTariffEstimateStore, query: DefaultTariffEstimateQuery): MutableList<DefaultTariffEstimate> {
    return query.apply(store.list())
}
