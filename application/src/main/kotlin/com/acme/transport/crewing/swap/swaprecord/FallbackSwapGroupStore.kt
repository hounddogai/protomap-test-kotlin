package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback swap groups by code. */
interface FallbackSwapGroupStore {
    fun find(code: String): FallbackSwapGroup?
    fun save(item: FallbackSwapGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackSwapGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackSwapGroupStore(
    var items: MutableMap<String, FallbackSwapGroup>,
    var order: MutableList<String>,
) : FallbackSwapGroupStore {
    override fun find(code: String): FallbackSwapGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackSwapGroup) {
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

    override fun list(): MutableList<FallbackSwapGroup> {
        val out = mutableListOf<FallbackSwapGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackSwapGroupStore(): FallbackSwapGroupStore {
    return MemoryFallbackSwapGroupStore(mutableMapOf<String, FallbackSwapGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackSwapGroupStore(
    var inner: FallbackSwapGroupStore,
    var cache: TtlCache<FallbackSwapGroup>,
    var hits: Int,
    var misses: Int,
) : FallbackSwapGroupStore {
    override fun find(code: String): FallbackSwapGroup? {
        val hit = (cache.get(code) as FallbackSwapGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackSwapGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackSwapGroup> = inner.list()
}

fun newCachedFallbackSwapGroupStore(inner: FallbackSwapGroupStore, ttl: Int, clock: Clock): FallbackSwapGroupStore {
    return CachedFallbackSwapGroupStore(
        inner = inner,
        cache = newTtlCache<FallbackSwapGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback swap groups. Empty fields match all. */
data class FallbackSwapGroupQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackSwapGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackSwapGroup>): MutableList<FallbackSwapGroup> {
        val matched = filter(items) { candidate: FallbackSwapGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackSwapGroups(store: FallbackSwapGroupStore, query: FallbackSwapGroupQuery): MutableList<FallbackSwapGroup> {
    return query.apply(store.list())
}
