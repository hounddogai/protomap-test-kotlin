package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient capacity requests by code. */
interface LenientCapacityRequestStore {
    fun find(code: String): LenientCapacityRequest?
    fun save(item: LenientCapacityRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCapacityRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCapacityRequestStore(
    var items: MutableMap<String, LenientCapacityRequest>,
    var order: MutableList<String>,
) : LenientCapacityRequestStore {
    override fun find(code: String): LenientCapacityRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCapacityRequest) {
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

    override fun list(): MutableList<LenientCapacityRequest> {
        val out = mutableListOf<LenientCapacityRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCapacityRequestStore(): LenientCapacityRequestStore {
    return MemoryLenientCapacityRequestStore(
        mutableMapOf<String, LenientCapacityRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCapacityRequestStore(
    var inner: LenientCapacityRequestStore,
    var cache: TtlCache<LenientCapacityRequest>,
    var hits: Int,
    var misses: Int,
) : LenientCapacityRequestStore {
    override fun find(code: String): LenientCapacityRequest? {
        val hit = (cache.get(code) as LenientCapacityRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCapacityRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCapacityRequest> = inner.list()
}

fun newCachedLenientCapacityRequestStore(inner: LenientCapacityRequestStore, ttl: Int, clock: Clock): LenientCapacityRequestStore {
    return CachedLenientCapacityRequestStore(
        inner = inner,
        cache = newTtlCache<LenientCapacityRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient capacity requests. Empty fields match all. */
data class LenientCapacityRequestQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCapacityRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientCapacityRequest>): MutableList<LenientCapacityRequest> {
        val matched = filter(items) { candidate: LenientCapacityRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCapacityRequests(store: LenientCapacityRequestStore, query: LenientCapacityRequestQuery): MutableList<LenientCapacityRequest> {
    return query.apply(store.list())
}
