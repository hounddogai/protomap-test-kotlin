package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tire requests by code. */
interface PrimaryTireRequestStore {
    fun find(code: String): PrimaryTireRequest?
    fun save(item: PrimaryTireRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTireRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTireRequestStore(
    var items: MutableMap<String, PrimaryTireRequest>,
    var order: MutableList<String>,
) : PrimaryTireRequestStore {
    override fun find(code: String): PrimaryTireRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTireRequest) {
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

    override fun list(): MutableList<PrimaryTireRequest> {
        val out = mutableListOf<PrimaryTireRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTireRequestStore(): PrimaryTireRequestStore {
    return MemoryPrimaryTireRequestStore(mutableMapOf<String, PrimaryTireRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTireRequestStore(
    var inner: PrimaryTireRequestStore,
    var cache: TtlCache<PrimaryTireRequest>,
    var hits: Int,
    var misses: Int,
) : PrimaryTireRequestStore {
    override fun find(code: String): PrimaryTireRequest? {
        val hit = (cache.get(code) as PrimaryTireRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTireRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTireRequest> = inner.list()
}

fun newCachedPrimaryTireRequestStore(inner: PrimaryTireRequestStore, ttl: Int, clock: Clock): PrimaryTireRequestStore {
    return CachedPrimaryTireRequestStore(
        inner = inner,
        cache = newTtlCache<PrimaryTireRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tire requests. Empty fields match all. */
data class PrimaryTireRequestQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTireRequest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTireRequest>): MutableList<PrimaryTireRequest> {
        val matched = filter(items) { candidate: PrimaryTireRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTireRequests(store: PrimaryTireRequestStore, query: PrimaryTireRequestQuery): MutableList<PrimaryTireRequest> {
    return query.apply(store.list())
}
