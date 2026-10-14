package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal fuel surcharge batches by code. */
interface SeasonalFuelSurchargeBatchStore {
    fun find(code: String): SeasonalFuelSurchargeBatch?
    fun save(item: SeasonalFuelSurchargeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFuelSurchargeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFuelSurchargeBatchStore(
    var items: MutableMap<String, SeasonalFuelSurchargeBatch>,
    var order: MutableList<String>,
) : SeasonalFuelSurchargeBatchStore {
    override fun find(code: String): SeasonalFuelSurchargeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFuelSurchargeBatch) {
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

    override fun list(): MutableList<SeasonalFuelSurchargeBatch> {
        val out = mutableListOf<SeasonalFuelSurchargeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFuelSurchargeBatchStore(): SeasonalFuelSurchargeBatchStore {
    return MemorySeasonalFuelSurchargeBatchStore(
        mutableMapOf<String, SeasonalFuelSurchargeBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFuelSurchargeBatchStore(
    var inner: SeasonalFuelSurchargeBatchStore,
    var cache: TtlCache<SeasonalFuelSurchargeBatch>,
    var hits: Int,
    var misses: Int,
) : SeasonalFuelSurchargeBatchStore {
    override fun find(code: String): SeasonalFuelSurchargeBatch? {
        val hit = (cache.get(code) as SeasonalFuelSurchargeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFuelSurchargeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFuelSurchargeBatch> = inner.list()
}

fun newCachedSeasonalFuelSurchargeBatchStore(inner: SeasonalFuelSurchargeBatchStore, ttl: Int, clock: Clock): SeasonalFuelSurchargeBatchStore {
    return CachedSeasonalFuelSurchargeBatchStore(
        inner = inner,
        cache = newTtlCache<SeasonalFuelSurchargeBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal fuel surcharge batches. Empty fields match all. */
data class SeasonalFuelSurchargeBatchQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFuelSurchargeBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFuelSurchargeBatch>): MutableList<SeasonalFuelSurchargeBatch> {
        val matched = filter(items) { candidate: SeasonalFuelSurchargeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFuelSurchargeBatches(store: SeasonalFuelSurchargeBatchStore, query: SeasonalFuelSurchargeBatchQuery): MutableList<SeasonalFuelSurchargeBatch> {
    return query.apply(store.list())
}
