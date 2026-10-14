package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization requests by code. */
interface UtilizationRequestStore {
    fun find(code: String): UtilizationRequest?
    fun save(item: UtilizationRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationRequestStore(
    var items: MutableMap<String, UtilizationRequest>,
    var order: MutableList<String>,
) : UtilizationRequestStore {
    override fun find(code: String): UtilizationRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationRequest) {
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

    override fun list(): MutableList<UtilizationRequest> {
        val out = mutableListOf<UtilizationRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationRequestStore(): UtilizationRequestStore {
    return MemoryUtilizationRequestStore(mutableMapOf<String, UtilizationRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationRequestStore(
    var inner: UtilizationRequestStore,
    var cache: TtlCache<UtilizationRequest>,
    var hits: Int,
    var misses: Int,
) : UtilizationRequestStore {
    override fun find(code: String): UtilizationRequest? {
        val hit = (cache.get(code) as UtilizationRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationRequest> = inner.list()
}

fun newCachedUtilizationRequestStore(inner: UtilizationRequestStore, ttl: Int, clock: Clock): UtilizationRequestStore {
    return CachedUtilizationRequestStore(
        inner = inner,
        cache = newTtlCache<UtilizationRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization requests. Empty fields match all. */
data class UtilizationRequestQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationRequest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationRequest>): MutableList<UtilizationRequest> {
        val matched = filter(items) { candidate: UtilizationRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationRequests(store: UtilizationRequestStore, query: UtilizationRequestQuery): MutableList<UtilizationRequest> {
    return query.apply(store.list())
}
