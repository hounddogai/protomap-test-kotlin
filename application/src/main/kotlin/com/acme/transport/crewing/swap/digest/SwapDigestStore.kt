package com.acme.transport.crewing.swap.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap digests by code. */
interface SwapDigestStore {
    fun find(code: String): SwapDigest?
    fun save(item: SwapDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapDigestStore(
    var items: MutableMap<String, SwapDigest>,
    var order: MutableList<String>,
) : SwapDigestStore {
    override fun find(code: String): SwapDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapDigest) {
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

    override fun list(): MutableList<SwapDigest> {
        val out = mutableListOf<SwapDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapDigestStore(): SwapDigestStore {
    return MemorySwapDigestStore(mutableMapOf<String, SwapDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapDigestStore(
    var inner: SwapDigestStore,
    var cache: TtlCache<SwapDigest>,
    var hits: Int,
    var misses: Int,
) : SwapDigestStore {
    override fun find(code: String): SwapDigest? {
        val hit = (cache.get(code) as SwapDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapDigest> = inner.list()
}

fun newCachedSwapDigestStore(inner: SwapDigestStore, ttl: Int, clock: Clock): SwapDigestStore {
    return CachedSwapDigestStore(
        inner = inner,
        cache = newTtlCache<SwapDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap digests. Empty fields match all. */
data class SwapDigestQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapDigest>): MutableList<SwapDigest> {
        val matched = filter(items) { candidate: SwapDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapDigests(store: SwapDigestStore, query: SwapDigestQuery): MutableList<SwapDigest> {
    return query.apply(store.list())
}
