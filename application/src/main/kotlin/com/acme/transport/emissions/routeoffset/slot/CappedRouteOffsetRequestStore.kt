package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped route offset requests by code. */
interface CappedRouteOffsetRequestStore {
    fun find(code: String): CappedRouteOffsetRequest?
    fun save(item: CappedRouteOffsetRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRouteOffsetRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRouteOffsetRequestStore(
    var items: MutableMap<String, CappedRouteOffsetRequest>,
    var order: MutableList<String>,
) : CappedRouteOffsetRequestStore {
    override fun find(code: String): CappedRouteOffsetRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRouteOffsetRequest) {
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

    override fun list(): MutableList<CappedRouteOffsetRequest> {
        val out = mutableListOf<CappedRouteOffsetRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRouteOffsetRequestStore(): CappedRouteOffsetRequestStore {
    return MemoryCappedRouteOffsetRequestStore(
        mutableMapOf<String, CappedRouteOffsetRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRouteOffsetRequestStore(
    var inner: CappedRouteOffsetRequestStore,
    var cache: TtlCache<CappedRouteOffsetRequest>,
    var hits: Int,
    var misses: Int,
) : CappedRouteOffsetRequestStore {
    override fun find(code: String): CappedRouteOffsetRequest? {
        val hit = (cache.get(code) as CappedRouteOffsetRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRouteOffsetRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRouteOffsetRequest> = inner.list()
}

fun newCachedCappedRouteOffsetRequestStore(inner: CappedRouteOffsetRequestStore, ttl: Int, clock: Clock): CappedRouteOffsetRequestStore {
    return CachedCappedRouteOffsetRequestStore(
        inner = inner,
        cache = newTtlCache<CappedRouteOffsetRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped route offset requests. Empty fields match all. */
data class CappedRouteOffsetRequestQuery(
    var source: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRouteOffsetRequest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRouteOffsetRequest>): MutableList<CappedRouteOffsetRequest> {
        val matched = filter(items) { candidate: CappedRouteOffsetRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRouteOffsetRequests(store: CappedRouteOffsetRequestStore, query: CappedRouteOffsetRequestQuery): MutableList<CappedRouteOffsetRequest> {
    return query.apply(store.list())
}
