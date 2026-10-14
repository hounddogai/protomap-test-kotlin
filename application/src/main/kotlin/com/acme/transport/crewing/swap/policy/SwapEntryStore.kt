package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap entries by code. */
interface SwapEntryStore {
    fun find(code: String): SwapEntry?
    fun save(item: SwapEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapEntryStore(
    var items: MutableMap<String, SwapEntry>,
    var order: MutableList<String>,
) : SwapEntryStore {
    override fun find(code: String): SwapEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapEntry) {
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

    override fun list(): MutableList<SwapEntry> {
        val out = mutableListOf<SwapEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapEntryStore(): SwapEntryStore {
    return MemorySwapEntryStore(mutableMapOf<String, SwapEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapEntryStore(
    var inner: SwapEntryStore,
    var cache: TtlCache<SwapEntry>,
    var hits: Int,
    var misses: Int,
) : SwapEntryStore {
    override fun find(code: String): SwapEntry? {
        val hit = (cache.get(code) as SwapEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapEntry> = inner.list()
}

fun newCachedSwapEntryStore(inner: SwapEntryStore, ttl: Int, clock: Clock): SwapEntryStore {
    return CachedSwapEntryStore(
        inner = inner,
        cache = newTtlCache<SwapEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap entries. Empty fields match all. */
data class SwapEntryQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapEntry>): MutableList<SwapEntry> {
        val matched = filter(items) { candidate: SwapEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapEntries(store: SwapEntryStore, query: SwapEntryQuery): MutableList<SwapEntry> {
    return query.apply(store.list())
}
