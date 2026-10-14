package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery forecasts by code. */
interface ProofOfDeliveryForecastStore {
    fun find(code: String): ProofOfDeliveryForecast?
    fun save(item: ProofOfDeliveryForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryForecastStore(
    var items: MutableMap<String, ProofOfDeliveryForecast>,
    var order: MutableList<String>,
) : ProofOfDeliveryForecastStore {
    override fun find(code: String): ProofOfDeliveryForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryForecast) {
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

    override fun list(): MutableList<ProofOfDeliveryForecast> {
        val out = mutableListOf<ProofOfDeliveryForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryForecastStore(): ProofOfDeliveryForecastStore {
    return MemoryProofOfDeliveryForecastStore(
        mutableMapOf<String, ProofOfDeliveryForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryForecastStore(
    var inner: ProofOfDeliveryForecastStore,
    var cache: TtlCache<ProofOfDeliveryForecast>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryForecastStore {
    override fun find(code: String): ProofOfDeliveryForecast? {
        val hit = (cache.get(code) as ProofOfDeliveryForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryForecast> = inner.list()
}

fun newCachedProofOfDeliveryForecastStore(inner: ProofOfDeliveryForecastStore, ttl: Int, clock: Clock): ProofOfDeliveryForecastStore {
    return CachedProofOfDeliveryForecastStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery forecasts. Empty fields match all. */
data class ProofOfDeliveryForecastQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ProofOfDeliveryForecast>): MutableList<ProofOfDeliveryForecast> {
        val matched = filter(items) { candidate: ProofOfDeliveryForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryForecasts(store: ProofOfDeliveryForecastStore, query: ProofOfDeliveryForecastQuery): MutableList<ProofOfDeliveryForecast> {
    return query.apply(store.list())
}
