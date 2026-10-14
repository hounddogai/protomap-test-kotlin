package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap variances by code. */
interface SwapVarianceStore {
    fun find(code: String): SwapVariance?
    fun save(item: SwapVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapVarianceStore(
    var items: MutableMap<String, SwapVariance>,
    var order: MutableList<String>,
) : SwapVarianceStore {
    override fun find(code: String): SwapVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapVariance) {
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

    override fun list(): MutableList<SwapVariance> {
        val out = mutableListOf<SwapVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapVarianceStore(): SwapVarianceStore {
    return MemorySwapVarianceStore(mutableMapOf<String, SwapVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapVarianceStore(
    var inner: SwapVarianceStore,
    var cache: TtlCache<SwapVariance>,
    var hits: Int,
    var misses: Int,
) : SwapVarianceStore {
    override fun find(code: String): SwapVariance? {
        val hit = (cache.get(code) as SwapVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapVariance> = inner.list()
}

fun newCachedSwapVarianceStore(inner: SwapVarianceStore, ttl: Int, clock: Clock): SwapVarianceStore {
    return CachedSwapVarianceStore(
        inner = inner,
        cache = newTtlCache<SwapVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap variances. Empty fields match all. */
data class SwapVarianceQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapVariance>): MutableList<SwapVariance> {
        val matched = filter(items) { candidate: SwapVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapVariances(store: SwapVarianceStore, query: SwapVarianceQuery): MutableList<SwapVariance> {
    return query.apply(store.list())
}
