package com.acme.transport.crewing.swap.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap baselines by code. */
interface SwapBaselineStore {
    fun find(code: String): SwapBaseline?
    fun save(item: SwapBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapBaselineStore(
    var items: MutableMap<String, SwapBaseline>,
    var order: MutableList<String>,
) : SwapBaselineStore {
    override fun find(code: String): SwapBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapBaseline) {
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

    override fun list(): MutableList<SwapBaseline> {
        val out = mutableListOf<SwapBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapBaselineStore(): SwapBaselineStore {
    return MemorySwapBaselineStore(mutableMapOf<String, SwapBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapBaselineStore(
    var inner: SwapBaselineStore,
    var cache: TtlCache<SwapBaseline>,
    var hits: Int,
    var misses: Int,
) : SwapBaselineStore {
    override fun find(code: String): SwapBaseline? {
        val hit = (cache.get(code) as SwapBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapBaseline> = inner.list()
}

fun newCachedSwapBaselineStore(inner: SwapBaselineStore, ttl: Int, clock: Clock): SwapBaselineStore {
    return CachedSwapBaselineStore(
        inner = inner,
        cache = newTtlCache<SwapBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap baselines. Empty fields match all. */
data class SwapBaselineQuery(
    var locale: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapBaseline): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapBaseline>): MutableList<SwapBaseline> {
        val matched = filter(items) { candidate: SwapBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapBaselines(store: SwapBaselineStore, query: SwapBaselineQuery): MutableList<SwapBaseline> {
    return query.apply(store.list())
}
