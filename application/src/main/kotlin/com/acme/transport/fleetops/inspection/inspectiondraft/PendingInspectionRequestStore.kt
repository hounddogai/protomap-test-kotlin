package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection requests by code. */
interface PendingInspectionRequestStore {
    fun find(code: String): PendingInspectionRequest?
    fun save(item: PendingInspectionRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionRequestStore(
    var items: MutableMap<String, PendingInspectionRequest>,
    var order: MutableList<String>,
) : PendingInspectionRequestStore {
    override fun find(code: String): PendingInspectionRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionRequest) {
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

    override fun list(): MutableList<PendingInspectionRequest> {
        val out = mutableListOf<PendingInspectionRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionRequestStore(): PendingInspectionRequestStore {
    return MemoryPendingInspectionRequestStore(
        mutableMapOf<String, PendingInspectionRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionRequestStore(
    var inner: PendingInspectionRequestStore,
    var cache: TtlCache<PendingInspectionRequest>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionRequestStore {
    override fun find(code: String): PendingInspectionRequest? {
        val hit = (cache.get(code) as PendingInspectionRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionRequest> = inner.list()
}

fun newCachedPendingInspectionRequestStore(inner: PendingInspectionRequestStore, ttl: Int, clock: Clock): PendingInspectionRequestStore {
    return CachedPendingInspectionRequestStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection requests. Empty fields match all. */
data class PendingInspectionRequestQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionRequest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionRequest>): MutableList<PendingInspectionRequest> {
        val matched = filter(items) { candidate: PendingInspectionRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionRequests(store: PendingInspectionRequestStore, query: PendingInspectionRequestQuery): MutableList<PendingInspectionRequest> {
    return query.apply(store.list())
}
