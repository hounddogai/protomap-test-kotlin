package com.acme.transport.safety.incident.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback incident requests by code. */
interface FallbackIncidentRequestStore {
    fun find(code: String): FallbackIncidentRequest?
    fun save(item: FallbackIncidentRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackIncidentRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackIncidentRequestStore(
    var items: MutableMap<String, FallbackIncidentRequest>,
    var order: MutableList<String>,
) : FallbackIncidentRequestStore {
    override fun find(code: String): FallbackIncidentRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackIncidentRequest) {
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

    override fun list(): MutableList<FallbackIncidentRequest> {
        val out = mutableListOf<FallbackIncidentRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackIncidentRequestStore(): FallbackIncidentRequestStore {
    return MemoryFallbackIncidentRequestStore(
        mutableMapOf<String, FallbackIncidentRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackIncidentRequestStore(
    var inner: FallbackIncidentRequestStore,
    var cache: TtlCache<FallbackIncidentRequest>,
    var hits: Int,
    var misses: Int,
) : FallbackIncidentRequestStore {
    override fun find(code: String): FallbackIncidentRequest? {
        val hit = (cache.get(code) as FallbackIncidentRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackIncidentRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackIncidentRequest> = inner.list()
}

fun newCachedFallbackIncidentRequestStore(inner: FallbackIncidentRequestStore, ttl: Int, clock: Clock): FallbackIncidentRequestStore {
    return CachedFallbackIncidentRequestStore(
        inner = inner,
        cache = newTtlCache<FallbackIncidentRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback incident requests. Empty fields match all. */
data class FallbackIncidentRequestQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackIncidentRequest): Boolean {
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

    fun apply(items: MutableList<FallbackIncidentRequest>): MutableList<FallbackIncidentRequest> {
        val matched = filter(items) { candidate: FallbackIncidentRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackIncidentRequests(store: FallbackIncidentRequestStore, query: FallbackIncidentRequestQuery): MutableList<FallbackIncidentRequest> {
    return query.apply(store.list())
}
