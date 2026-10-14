package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge requests by code. */
interface FuelSurchargeRequestStore {
    fun find(code: String): FuelSurchargeRequest?
    fun save(item: FuelSurchargeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeRequestStore(
    var items: MutableMap<String, FuelSurchargeRequest>,
    var order: MutableList<String>,
) : FuelSurchargeRequestStore {
    override fun find(code: String): FuelSurchargeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeRequest) {
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

    override fun list(): MutableList<FuelSurchargeRequest> {
        val out = mutableListOf<FuelSurchargeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeRequestStore(): FuelSurchargeRequestStore {
    return MemoryFuelSurchargeRequestStore(
        mutableMapOf<String, FuelSurchargeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeRequestStore(
    var inner: FuelSurchargeRequestStore,
    var cache: TtlCache<FuelSurchargeRequest>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeRequestStore {
    override fun find(code: String): FuelSurchargeRequest? {
        val hit = (cache.get(code) as FuelSurchargeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeRequest> = inner.list()
}

fun newCachedFuelSurchargeRequestStore(inner: FuelSurchargeRequestStore, ttl: Int, clock: Clock): FuelSurchargeRequestStore {
    return CachedFuelSurchargeRequestStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge requests. Empty fields match all. */
data class FuelSurchargeRequestQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeRequest>): MutableList<FuelSurchargeRequest> {
        val matched = filter(items) { candidate: FuelSurchargeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeRequests(store: FuelSurchargeRequestStore, query: FuelSurchargeRequestQuery): MutableList<FuelSurchargeRequest> {
    return query.apply(store.list())
}
