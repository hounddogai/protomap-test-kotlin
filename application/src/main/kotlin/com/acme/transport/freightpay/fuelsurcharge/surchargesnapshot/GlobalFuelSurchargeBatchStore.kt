package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel surcharge batches by code. */
interface GlobalFuelSurchargeBatchStore {
    fun find(code: String): GlobalFuelSurchargeBatch?
    fun save(item: GlobalFuelSurchargeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelSurchargeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelSurchargeBatchStore(
    var items: MutableMap<String, GlobalFuelSurchargeBatch>,
    var order: MutableList<String>,
) : GlobalFuelSurchargeBatchStore {
    override fun find(code: String): GlobalFuelSurchargeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelSurchargeBatch) {
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

    override fun list(): MutableList<GlobalFuelSurchargeBatch> {
        val out = mutableListOf<GlobalFuelSurchargeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelSurchargeBatchStore(): GlobalFuelSurchargeBatchStore {
    return MemoryGlobalFuelSurchargeBatchStore(
        mutableMapOf<String, GlobalFuelSurchargeBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelSurchargeBatchStore(
    var inner: GlobalFuelSurchargeBatchStore,
    var cache: TtlCache<GlobalFuelSurchargeBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelSurchargeBatchStore {
    override fun find(code: String): GlobalFuelSurchargeBatch? {
        val hit = (cache.get(code) as GlobalFuelSurchargeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelSurchargeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelSurchargeBatch> = inner.list()
}

fun newCachedGlobalFuelSurchargeBatchStore(inner: GlobalFuelSurchargeBatchStore, ttl: Int, clock: Clock): GlobalFuelSurchargeBatchStore {
    return CachedGlobalFuelSurchargeBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelSurchargeBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel surcharge batches. Empty fields match all. */
data class GlobalFuelSurchargeBatchQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelSurchargeBatch): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<GlobalFuelSurchargeBatch>): MutableList<GlobalFuelSurchargeBatch> {
        val matched = filter(items) { candidate: GlobalFuelSurchargeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelSurchargeBatches(store: GlobalFuelSurchargeBatchStore, query: GlobalFuelSurchargeBatchQuery): MutableList<GlobalFuelSurchargeBatch> {
    return query.apply(store.list())
}
