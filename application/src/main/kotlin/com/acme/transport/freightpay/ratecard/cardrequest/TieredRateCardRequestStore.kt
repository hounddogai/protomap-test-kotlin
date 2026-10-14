package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rate card requests by code. */
interface TieredRateCardRequestStore {
    fun find(code: String): TieredRateCardRequest?
    fun save(item: TieredRateCardRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRateCardRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRateCardRequestStore(
    var items: MutableMap<String, TieredRateCardRequest>,
    var order: MutableList<String>,
) : TieredRateCardRequestStore {
    override fun find(code: String): TieredRateCardRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRateCardRequest) {
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

    override fun list(): MutableList<TieredRateCardRequest> {
        val out = mutableListOf<TieredRateCardRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRateCardRequestStore(): TieredRateCardRequestStore {
    return MemoryTieredRateCardRequestStore(
        mutableMapOf<String, TieredRateCardRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRateCardRequestStore(
    var inner: TieredRateCardRequestStore,
    var cache: TtlCache<TieredRateCardRequest>,
    var hits: Int,
    var misses: Int,
) : TieredRateCardRequestStore {
    override fun find(code: String): TieredRateCardRequest? {
        val hit = (cache.get(code) as TieredRateCardRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRateCardRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRateCardRequest> = inner.list()
}

fun newCachedTieredRateCardRequestStore(inner: TieredRateCardRequestStore, ttl: Int, clock: Clock): TieredRateCardRequestStore {
    return CachedTieredRateCardRequestStore(
        inner = inner,
        cache = newTtlCache<TieredRateCardRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rate card requests. Empty fields match all. */
data class TieredRateCardRequestQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRateCardRequest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRateCardRequest>): MutableList<TieredRateCardRequest> {
        val matched = filter(items) { candidate: TieredRateCardRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRateCardRequests(store: TieredRateCardRequestStore, query: TieredRateCardRequestQuery): MutableList<TieredRateCardRequest> {
    return query.apply(store.list())
}
