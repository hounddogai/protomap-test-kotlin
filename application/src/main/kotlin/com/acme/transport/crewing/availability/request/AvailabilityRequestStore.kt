package com.acme.transport.crewing.availability.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability requests by code. */
interface AvailabilityRequestStore {
    fun find(code: String): AvailabilityRequest?
    fun save(item: AvailabilityRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityRequestStore(
    var items: MutableMap<String, AvailabilityRequest>,
    var order: MutableList<String>,
) : AvailabilityRequestStore {
    override fun find(code: String): AvailabilityRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityRequest) {
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

    override fun list(): MutableList<AvailabilityRequest> {
        val out = mutableListOf<AvailabilityRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityRequestStore(): AvailabilityRequestStore {
    return MemoryAvailabilityRequestStore(mutableMapOf<String, AvailabilityRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityRequestStore(
    var inner: AvailabilityRequestStore,
    var cache: TtlCache<AvailabilityRequest>,
    var hits: Int,
    var misses: Int,
) : AvailabilityRequestStore {
    override fun find(code: String): AvailabilityRequest? {
        val hit = (cache.get(code) as AvailabilityRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityRequest> = inner.list()
}

fun newCachedAvailabilityRequestStore(inner: AvailabilityRequestStore, ttl: Int, clock: Clock): AvailabilityRequestStore {
    return CachedAvailabilityRequestStore(
        inner = inner,
        cache = newTtlCache<AvailabilityRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability requests. Empty fields match all. */
data class AvailabilityRequestQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityRequest>): MutableList<AvailabilityRequest> {
        val matched = filter(items) { candidate: AvailabilityRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityRequests(store: AvailabilityRequestStore, query: AvailabilityRequestQuery): MutableList<AvailabilityRequest> {
    return query.apply(store.list())
}
