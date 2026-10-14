package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge batches by code. */
interface FuelSurchargeBatchStore {
    fun find(code: String): FuelSurchargeBatch?
    fun save(item: FuelSurchargeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeBatchStore(
    var items: MutableMap<String, FuelSurchargeBatch>,
    var order: MutableList<String>,
) : FuelSurchargeBatchStore {
    override fun find(code: String): FuelSurchargeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeBatch) {
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

    override fun list(): MutableList<FuelSurchargeBatch> {
        val out = mutableListOf<FuelSurchargeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeBatchStore(): FuelSurchargeBatchStore {
    return MemoryFuelSurchargeBatchStore(mutableMapOf<String, FuelSurchargeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeBatchStore(
    var inner: FuelSurchargeBatchStore,
    var cache: TtlCache<FuelSurchargeBatch>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeBatchStore {
    override fun find(code: String): FuelSurchargeBatch? {
        val hit = (cache.get(code) as FuelSurchargeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeBatch> = inner.list()
}

fun newCachedFuelSurchargeBatchStore(inner: FuelSurchargeBatchStore, ttl: Int, clock: Clock): FuelSurchargeBatchStore {
    return CachedFuelSurchargeBatchStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge batches. Empty fields match all. */
data class FuelSurchargeBatchQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeBatch>): MutableList<FuelSurchargeBatch> {
        val matched = filter(items) { candidate: FuelSurchargeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeBatches(store: FuelSurchargeBatchStore, query: FuelSurchargeBatchQuery): MutableList<FuelSurchargeBatch> {
    return query.apply(store.list())
}
