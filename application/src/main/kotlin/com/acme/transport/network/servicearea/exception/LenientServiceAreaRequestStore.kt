package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient service area requests by code. */
interface LenientServiceAreaRequestStore {
    fun find(code: String): LenientServiceAreaRequest?
    fun save(item: LenientServiceAreaRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServiceAreaRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServiceAreaRequestStore(
    var items: MutableMap<String, LenientServiceAreaRequest>,
    var order: MutableList<String>,
) : LenientServiceAreaRequestStore {
    override fun find(code: String): LenientServiceAreaRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServiceAreaRequest) {
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

    override fun list(): MutableList<LenientServiceAreaRequest> {
        val out = mutableListOf<LenientServiceAreaRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServiceAreaRequestStore(): LenientServiceAreaRequestStore {
    return MemoryLenientServiceAreaRequestStore(
        mutableMapOf<String, LenientServiceAreaRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServiceAreaRequestStore(
    var inner: LenientServiceAreaRequestStore,
    var cache: TtlCache<LenientServiceAreaRequest>,
    var hits: Int,
    var misses: Int,
) : LenientServiceAreaRequestStore {
    override fun find(code: String): LenientServiceAreaRequest? {
        val hit = (cache.get(code) as LenientServiceAreaRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServiceAreaRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServiceAreaRequest> = inner.list()
}

fun newCachedLenientServiceAreaRequestStore(inner: LenientServiceAreaRequestStore, ttl: Int, clock: Clock): LenientServiceAreaRequestStore {
    return CachedLenientServiceAreaRequestStore(
        inner = inner,
        cache = newTtlCache<LenientServiceAreaRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service area requests. Empty fields match all. */
data class LenientServiceAreaRequestQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServiceAreaRequest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServiceAreaRequest>): MutableList<LenientServiceAreaRequest> {
        val matched = filter(items) { candidate: LenientServiceAreaRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServiceAreaRequests(store: LenientServiceAreaRequestStore, query: LenientServiceAreaRequestQuery): MutableList<LenientServiceAreaRequest> {
    return query.apply(store.list())
}
