package com.acme.transport.crewing.swap.group

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap groups by code. */
interface SwapGroupStore {
    fun find(code: String): SwapGroup?
    fun save(item: SwapGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapGroupStore(
    var items: MutableMap<String, SwapGroup>,
    var order: MutableList<String>,
) : SwapGroupStore {
    override fun find(code: String): SwapGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapGroup) {
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

    override fun list(): MutableList<SwapGroup> {
        val out = mutableListOf<SwapGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapGroupStore(): SwapGroupStore {
    return MemorySwapGroupStore(mutableMapOf<String, SwapGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapGroupStore(
    var inner: SwapGroupStore,
    var cache: TtlCache<SwapGroup>,
    var hits: Int,
    var misses: Int,
) : SwapGroupStore {
    override fun find(code: String): SwapGroup? {
        val hit = (cache.get(code) as SwapGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapGroup> = inner.list()
}

fun newCachedSwapGroupStore(inner: SwapGroupStore, ttl: Int, clock: Clock): SwapGroupStore {
    return CachedSwapGroupStore(
        inner = inner,
        cache = newTtlCache<SwapGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap groups. Empty fields match all. */
data class SwapGroupQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapGroup>): MutableList<SwapGroup> {
        val matched = filter(items) { candidate: SwapGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapGroups(store: SwapGroupStore, query: SwapGroupQuery): MutableList<SwapGroup> {
    return query.apply(store.list())
}
