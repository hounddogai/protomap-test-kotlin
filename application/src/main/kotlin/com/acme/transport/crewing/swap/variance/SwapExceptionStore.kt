package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap exceptions by code. */
interface SwapExceptionStore {
    fun find(code: String): SwapException?
    fun save(item: SwapException)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapExceptionStore(
    var items: MutableMap<String, SwapException>,
    var order: MutableList<String>,
) : SwapExceptionStore {
    override fun find(code: String): SwapException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapException) {
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

    override fun list(): MutableList<SwapException> {
        val out = mutableListOf<SwapException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapExceptionStore(): SwapExceptionStore {
    return MemorySwapExceptionStore(mutableMapOf<String, SwapException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapExceptionStore(
    var inner: SwapExceptionStore,
    var cache: TtlCache<SwapException>,
    var hits: Int,
    var misses: Int,
) : SwapExceptionStore {
    override fun find(code: String): SwapException? {
        val hit = (cache.get(code) as SwapException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapException> = inner.list()
}

fun newCachedSwapExceptionStore(inner: SwapExceptionStore, ttl: Int, clock: Clock): SwapExceptionStore {
    return CachedSwapExceptionStore(
        inner = inner,
        cache = newTtlCache<SwapException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap exceptions. Empty fields match all. */
data class SwapExceptionQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapException): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapException>): MutableList<SwapException> {
        val matched = filter(items) { candidate: SwapException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapExceptions(store: SwapExceptionStore, query: SwapExceptionQuery): MutableList<SwapException> {
    return query.apply(store.list())
}
