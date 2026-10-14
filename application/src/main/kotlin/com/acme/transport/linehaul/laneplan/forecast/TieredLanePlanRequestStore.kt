package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered lane plan requests by code. */
interface TieredLanePlanRequestStore {
    fun find(code: String): TieredLanePlanRequest?
    fun save(item: TieredLanePlanRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredLanePlanRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredLanePlanRequestStore(
    var items: MutableMap<String, TieredLanePlanRequest>,
    var order: MutableList<String>,
) : TieredLanePlanRequestStore {
    override fun find(code: String): TieredLanePlanRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredLanePlanRequest) {
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

    override fun list(): MutableList<TieredLanePlanRequest> {
        val out = mutableListOf<TieredLanePlanRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredLanePlanRequestStore(): TieredLanePlanRequestStore {
    return MemoryTieredLanePlanRequestStore(
        mutableMapOf<String, TieredLanePlanRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredLanePlanRequestStore(
    var inner: TieredLanePlanRequestStore,
    var cache: TtlCache<TieredLanePlanRequest>,
    var hits: Int,
    var misses: Int,
) : TieredLanePlanRequestStore {
    override fun find(code: String): TieredLanePlanRequest? {
        val hit = (cache.get(code) as TieredLanePlanRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredLanePlanRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredLanePlanRequest> = inner.list()
}

fun newCachedTieredLanePlanRequestStore(inner: TieredLanePlanRequestStore, ttl: Int, clock: Clock): TieredLanePlanRequestStore {
    return CachedTieredLanePlanRequestStore(
        inner = inner,
        cache = newTtlCache<TieredLanePlanRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered lane plan requests. Empty fields match all. */
data class TieredLanePlanRequestQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredLanePlanRequest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredLanePlanRequest>): MutableList<TieredLanePlanRequest> {
        val matched = filter(items) { candidate: TieredLanePlanRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredLanePlanRequests(store: TieredLanePlanRequestStore, query: TieredLanePlanRequestQuery): MutableList<TieredLanePlanRequest> {
    return query.apply(store.list())
}
