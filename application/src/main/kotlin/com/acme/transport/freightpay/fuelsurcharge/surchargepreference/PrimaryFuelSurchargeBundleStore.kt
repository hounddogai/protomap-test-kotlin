package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel surcharge bundles by code. */
interface PrimaryFuelSurchargeBundleStore {
    fun find(code: String): PrimaryFuelSurchargeBundle?
    fun save(item: PrimaryFuelSurchargeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelSurchargeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelSurchargeBundleStore(
    var items: MutableMap<String, PrimaryFuelSurchargeBundle>,
    var order: MutableList<String>,
) : PrimaryFuelSurchargeBundleStore {
    override fun find(code: String): PrimaryFuelSurchargeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelSurchargeBundle) {
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

    override fun list(): MutableList<PrimaryFuelSurchargeBundle> {
        val out = mutableListOf<PrimaryFuelSurchargeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelSurchargeBundleStore(): PrimaryFuelSurchargeBundleStore {
    return MemoryPrimaryFuelSurchargeBundleStore(
        mutableMapOf<String, PrimaryFuelSurchargeBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelSurchargeBundleStore(
    var inner: PrimaryFuelSurchargeBundleStore,
    var cache: TtlCache<PrimaryFuelSurchargeBundle>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelSurchargeBundleStore {
    override fun find(code: String): PrimaryFuelSurchargeBundle? {
        val hit = (cache.get(code) as PrimaryFuelSurchargeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelSurchargeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelSurchargeBundle> = inner.list()
}

fun newCachedPrimaryFuelSurchargeBundleStore(inner: PrimaryFuelSurchargeBundleStore, ttl: Int, clock: Clock): PrimaryFuelSurchargeBundleStore {
    return CachedPrimaryFuelSurchargeBundleStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelSurchargeBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel surcharge bundles. Empty fields match all. */
data class PrimaryFuelSurchargeBundleQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelSurchargeBundle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFuelSurchargeBundle>): MutableList<PrimaryFuelSurchargeBundle> {
        val matched = filter(items) { candidate: PrimaryFuelSurchargeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelSurchargeBundles(store: PrimaryFuelSurchargeBundleStore, query: PrimaryFuelSurchargeBundleQuery): MutableList<PrimaryFuelSurchargeBundle> {
    return query.apply(store.list())
}
