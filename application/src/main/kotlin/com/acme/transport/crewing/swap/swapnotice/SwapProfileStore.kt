package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap profiles by code. */
interface SwapProfileStore {
    fun find(code: String): SwapProfile?
    fun save(item: SwapProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapProfileStore(
    var items: MutableMap<String, SwapProfile>,
    var order: MutableList<String>,
) : SwapProfileStore {
    override fun find(code: String): SwapProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapProfile) {
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

    override fun list(): MutableList<SwapProfile> {
        val out = mutableListOf<SwapProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapProfileStore(): SwapProfileStore {
    return MemorySwapProfileStore(mutableMapOf<String, SwapProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapProfileStore(
    var inner: SwapProfileStore,
    var cache: TtlCache<SwapProfile>,
    var hits: Int,
    var misses: Int,
) : SwapProfileStore {
    override fun find(code: String): SwapProfile? {
        val hit = (cache.get(code) as SwapProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapProfile> = inner.list()
}

fun newCachedSwapProfileStore(inner: SwapProfileStore, ttl: Int, clock: Clock): SwapProfileStore {
    return CachedSwapProfileStore(
        inner = inner,
        cache = newTtlCache<SwapProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap profiles. Empty fields match all. */
data class SwapProfileQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapProfile>): MutableList<SwapProfile> {
        val matched = filter(items) { candidate: SwapProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapProfiles(store: SwapProfileStore, query: SwapProfileQuery): MutableList<SwapProfile> {
    return query.apply(store.list())
}
