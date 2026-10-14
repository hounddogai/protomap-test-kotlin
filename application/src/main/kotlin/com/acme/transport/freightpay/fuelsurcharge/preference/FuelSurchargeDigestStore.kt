package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge digests by code. */
interface FuelSurchargeDigestStore {
    fun find(code: String): FuelSurchargeDigest?
    fun save(item: FuelSurchargeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeDigestStore(
    var items: MutableMap<String, FuelSurchargeDigest>,
    var order: MutableList<String>,
) : FuelSurchargeDigestStore {
    override fun find(code: String): FuelSurchargeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeDigest) {
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

    override fun list(): MutableList<FuelSurchargeDigest> {
        val out = mutableListOf<FuelSurchargeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeDigestStore(): FuelSurchargeDigestStore {
    return MemoryFuelSurchargeDigestStore(mutableMapOf<String, FuelSurchargeDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeDigestStore(
    var inner: FuelSurchargeDigestStore,
    var cache: TtlCache<FuelSurchargeDigest>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeDigestStore {
    override fun find(code: String): FuelSurchargeDigest? {
        val hit = (cache.get(code) as FuelSurchargeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeDigest> = inner.list()
}

fun newCachedFuelSurchargeDigestStore(inner: FuelSurchargeDigestStore, ttl: Int, clock: Clock): FuelSurchargeDigestStore {
    return CachedFuelSurchargeDigestStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge digests. Empty fields match all. */
data class FuelSurchargeDigestQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeDigest>): MutableList<FuelSurchargeDigest> {
        val matched = filter(items) { candidate: FuelSurchargeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeDigests(store: FuelSurchargeDigestStore, query: FuelSurchargeDigestQuery): MutableList<FuelSurchargeDigest> {
    return query.apply(store.list())
}
