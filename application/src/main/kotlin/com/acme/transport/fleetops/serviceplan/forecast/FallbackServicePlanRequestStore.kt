package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback service plan requests by code. */
interface FallbackServicePlanRequestStore {
    fun find(code: String): FallbackServicePlanRequest?
    fun save(item: FallbackServicePlanRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServicePlanRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServicePlanRequestStore(
    var items: MutableMap<String, FallbackServicePlanRequest>,
    var order: MutableList<String>,
) : FallbackServicePlanRequestStore {
    override fun find(code: String): FallbackServicePlanRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServicePlanRequest) {
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

    override fun list(): MutableList<FallbackServicePlanRequest> {
        val out = mutableListOf<FallbackServicePlanRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServicePlanRequestStore(): FallbackServicePlanRequestStore {
    return MemoryFallbackServicePlanRequestStore(
        mutableMapOf<String, FallbackServicePlanRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServicePlanRequestStore(
    var inner: FallbackServicePlanRequestStore,
    var cache: TtlCache<FallbackServicePlanRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackServicePlanRequestStore {
    override fun find(code: String): FallbackServicePlanRequest? {
        val hit = (cache.get(code) as FallbackServicePlanRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServicePlanRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServicePlanRequest> = inner.list()
}

fun newCachedFallbackServicePlanRequestStore(inner: FallbackServicePlanRequestStore, ttl: Int, clock: Clock): FallbackServicePlanRequestStore {
    return CachedFallbackServicePlanRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackServicePlanRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service plan requests. Empty fields match all. */
data class FallbackServicePlanRequestQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServicePlanRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FallbackServicePlanRequest>): MutableList<FallbackServicePlanRequest> {
        val matched = filter(items) { candidate: FallbackServicePlanRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServicePlanRequests(store: FallbackServicePlanRequestStore, query: FallbackServicePlanRequestQuery): MutableList<FallbackServicePlanRequest> {
    return query.apply(store.list())
}
