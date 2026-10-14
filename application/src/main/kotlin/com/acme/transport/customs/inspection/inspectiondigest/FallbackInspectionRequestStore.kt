package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback inspection requests by code. */
interface FallbackInspectionRequestStore {
    fun find(code: String): FallbackInspectionRequest?
    fun save(item: FallbackInspectionRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackInspectionRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackInspectionRequestStore(
    var items: MutableMap<String, FallbackInspectionRequest>,
    var order: MutableList<String>,
) : FallbackInspectionRequestStore {
    override fun find(code: String): FallbackInspectionRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackInspectionRequest) {
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

    override fun list(): MutableList<FallbackInspectionRequest> {
        val out = mutableListOf<FallbackInspectionRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackInspectionRequestStore(): FallbackInspectionRequestStore {
    return MemoryFallbackInspectionRequestStore(
        mutableMapOf<String, FallbackInspectionRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackInspectionRequestStore(
    var inner: FallbackInspectionRequestStore,
    var cache: TtlCache<FallbackInspectionRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackInspectionRequestStore {
    override fun find(code: String): FallbackInspectionRequest? {
        val hit = (cache.get(code) as FallbackInspectionRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackInspectionRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackInspectionRequest> = inner.list()
}

fun newCachedFallbackInspectionRequestStore(inner: FallbackInspectionRequestStore, ttl: Int, clock: Clock): FallbackInspectionRequestStore {
    return CachedFallbackInspectionRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackInspectionRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback inspection requests. Empty fields match all. */
data class FallbackInspectionRequestQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackInspectionRequest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackInspectionRequest>): MutableList<FallbackInspectionRequest> {
        val matched = filter(items) { candidate: FallbackInspectionRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackInspectionRequests(store: FallbackInspectionRequestStore, query: FallbackInspectionRequestQuery): MutableList<FallbackInspectionRequest> {
    return query.apply(store.list())
}
