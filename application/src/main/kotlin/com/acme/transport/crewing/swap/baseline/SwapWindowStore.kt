package com.acme.transport.crewing.swap.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap windows by code. */
interface SwapWindowStore {
    fun find(code: String): SwapWindow?
    fun save(item: SwapWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapWindowStore(
    var items: MutableMap<String, SwapWindow>,
    var order: MutableList<String>,
) : SwapWindowStore {
    override fun find(code: String): SwapWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapWindow) {
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

    override fun list(): MutableList<SwapWindow> {
        val out = mutableListOf<SwapWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapWindowStore(): SwapWindowStore {
    return MemorySwapWindowStore(mutableMapOf<String, SwapWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapWindowStore(
    var inner: SwapWindowStore,
    var cache: TtlCache<SwapWindow>,
    var hits: Int,
    var misses: Int,
) : SwapWindowStore {
    override fun find(code: String): SwapWindow? {
        val hit = (cache.get(code) as SwapWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapWindow> = inner.list()
}

fun newCachedSwapWindowStore(inner: SwapWindowStore, ttl: Int, clock: Clock): SwapWindowStore {
    return CachedSwapWindowStore(
        inner = inner,
        cache = newTtlCache<SwapWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap windows. Empty fields match all. */
data class SwapWindowQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapWindow): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapWindow>): MutableList<SwapWindow> {
        val matched = filter(items) { candidate: SwapWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapWindows(store: SwapWindowStore, query: SwapWindowQuery): MutableList<SwapWindow> {
    return query.apply(store.list())
}
