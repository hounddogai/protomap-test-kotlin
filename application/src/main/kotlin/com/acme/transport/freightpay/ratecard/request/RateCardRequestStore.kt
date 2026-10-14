package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card requests by code. */
interface RateCardRequestStore {
    fun find(code: String): RateCardRequest?
    fun save(item: RateCardRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardRequestStore(
    var items: MutableMap<String, RateCardRequest>,
    var order: MutableList<String>,
) : RateCardRequestStore {
    override fun find(code: String): RateCardRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardRequest) {
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

    override fun list(): MutableList<RateCardRequest> {
        val out = mutableListOf<RateCardRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardRequestStore(): RateCardRequestStore {
    return MemoryRateCardRequestStore(mutableMapOf<String, RateCardRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardRequestStore(
    var inner: RateCardRequestStore,
    var cache: TtlCache<RateCardRequest>,
    var hits: Int,
    var misses: Int,
) : RateCardRequestStore {
    override fun find(code: String): RateCardRequest? {
        val hit = (cache.get(code) as RateCardRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardRequest> = inner.list()
}

fun newCachedRateCardRequestStore(inner: RateCardRequestStore, ttl: Int, clock: Clock): RateCardRequestStore {
    return CachedRateCardRequestStore(
        inner = inner,
        cache = newTtlCache<RateCardRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card requests. Empty fields match all. */
data class RateCardRequestQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardRequest>): MutableList<RateCardRequest> {
        val matched = filter(items) { candidate: RateCardRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardRequests(store: RateCardRequestStore, query: RateCardRequestQuery): MutableList<RateCardRequest> {
    return query.apply(store.list())
}
