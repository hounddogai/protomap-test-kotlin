package com.acme.transport.crewing.swap.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict swap requests by code. */
interface StrictSwapRequestStore {
    fun find(code: String): StrictSwapRequest?
    fun save(item: StrictSwapRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictSwapRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictSwapRequestStore(
    var items: MutableMap<String, StrictSwapRequest>,
    var order: MutableList<String>,
) : StrictSwapRequestStore {
    override fun find(code: String): StrictSwapRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictSwapRequest) {
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

    override fun list(): MutableList<StrictSwapRequest> {
        val out = mutableListOf<StrictSwapRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictSwapRequestStore(): StrictSwapRequestStore {
    return MemoryStrictSwapRequestStore(mutableMapOf<String, StrictSwapRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictSwapRequestStore(
    var inner: StrictSwapRequestStore,
    var cache: TtlCache<StrictSwapRequest>,
    var hits: Int,
    var misses: Int,
) : StrictSwapRequestStore {
    override fun find(code: String): StrictSwapRequest? {
        val hit = (cache.get(code) as StrictSwapRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictSwapRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictSwapRequest> = inner.list()
}

fun newCachedStrictSwapRequestStore(inner: StrictSwapRequestStore, ttl: Int, clock: Clock): StrictSwapRequestStore {
    return CachedStrictSwapRequestStore(
        inner = inner,
        cache = newTtlCache<StrictSwapRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict swap requests. Empty fields match all. */
data class StrictSwapRequestQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictSwapRequest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<StrictSwapRequest>): MutableList<StrictSwapRequest> {
        val matched = filter(items) { candidate: StrictSwapRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictSwapRequests(store: StrictSwapRequestStore, query: StrictSwapRequestQuery): MutableList<StrictSwapRequest> {
    return query.apply(store.list())
}
