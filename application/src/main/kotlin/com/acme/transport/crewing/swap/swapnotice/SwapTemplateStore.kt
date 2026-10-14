package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap templates by code. */
interface SwapTemplateStore {
    fun find(code: String): SwapTemplate?
    fun save(item: SwapTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapTemplateStore(
    var items: MutableMap<String, SwapTemplate>,
    var order: MutableList<String>,
) : SwapTemplateStore {
    override fun find(code: String): SwapTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapTemplate) {
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

    override fun list(): MutableList<SwapTemplate> {
        val out = mutableListOf<SwapTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapTemplateStore(): SwapTemplateStore {
    return MemorySwapTemplateStore(mutableMapOf<String, SwapTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapTemplateStore(
    var inner: SwapTemplateStore,
    var cache: TtlCache<SwapTemplate>,
    var hits: Int,
    var misses: Int,
) : SwapTemplateStore {
    override fun find(code: String): SwapTemplate? {
        val hit = (cache.get(code) as SwapTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapTemplate> = inner.list()
}

fun newCachedSwapTemplateStore(inner: SwapTemplateStore, ttl: Int, clock: Clock): SwapTemplateStore {
    return CachedSwapTemplateStore(
        inner = inner,
        cache = newTtlCache<SwapTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap templates. Empty fields match all. */
data class SwapTemplateQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapTemplate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapTemplate>): MutableList<SwapTemplate> {
        val matched = filter(items) { candidate: SwapTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapTemplates(store: SwapTemplateStore, query: SwapTemplateQuery): MutableList<SwapTemplate> {
    return query.apply(store.list())
}
