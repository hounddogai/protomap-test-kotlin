package com.acme.transport.crewing.swap.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap mappings by code. */
interface SwapMappingStore {
    fun find(code: String): SwapMapping?
    fun save(item: SwapMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapMappingStore(
    var items: MutableMap<String, SwapMapping>,
    var order: MutableList<String>,
) : SwapMappingStore {
    override fun find(code: String): SwapMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapMapping) {
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

    override fun list(): MutableList<SwapMapping> {
        val out = mutableListOf<SwapMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapMappingStore(): SwapMappingStore {
    return MemorySwapMappingStore(mutableMapOf<String, SwapMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapMappingStore(
    var inner: SwapMappingStore,
    var cache: TtlCache<SwapMapping>,
    var hits: Int,
    var misses: Int,
) : SwapMappingStore {
    override fun find(code: String): SwapMapping? {
        val hit = (cache.get(code) as SwapMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapMapping> = inner.list()
}

fun newCachedSwapMappingStore(inner: SwapMappingStore, ttl: Int, clock: Clock): SwapMappingStore {
    return CachedSwapMappingStore(
        inner = inner,
        cache = newTtlCache<SwapMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap mappings. Empty fields match all. */
data class SwapMappingQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SwapMapping>): MutableList<SwapMapping> {
        val matched = filter(items) { candidate: SwapMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapMappings(store: SwapMappingStore, query: SwapMappingQuery): MutableList<SwapMapping> {
    return query.apply(store.list())
}
