package com.acme.transport.crewing.swap.budget

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap bundles by code. */
interface SwapBundleStore {
    fun find(code: String): SwapBundle?
    fun save(item: SwapBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapBundleStore(
    var items: MutableMap<String, SwapBundle>,
    var order: MutableList<String>,
) : SwapBundleStore {
    override fun find(code: String): SwapBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapBundle) {
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

    override fun list(): MutableList<SwapBundle> {
        val out = mutableListOf<SwapBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapBundleStore(): SwapBundleStore {
    return MemorySwapBundleStore(mutableMapOf<String, SwapBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapBundleStore(
    var inner: SwapBundleStore,
    var cache: TtlCache<SwapBundle>,
    var hits: Int,
    var misses: Int,
) : SwapBundleStore {
    override fun find(code: String): SwapBundle? {
        val hit = (cache.get(code) as SwapBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapBundle> = inner.list()
}

fun newCachedSwapBundleStore(inner: SwapBundleStore, ttl: Int, clock: Clock): SwapBundleStore {
    return CachedSwapBundleStore(
        inner = inner,
        cache = newTtlCache<SwapBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap bundles. Empty fields match all. */
data class SwapBundleQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapBundle>): MutableList<SwapBundle> {
        val matched = filter(items) { candidate: SwapBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapBundles(store: SwapBundleStore, query: SwapBundleQuery): MutableList<SwapBundle> {
    return query.apply(store.list())
}
