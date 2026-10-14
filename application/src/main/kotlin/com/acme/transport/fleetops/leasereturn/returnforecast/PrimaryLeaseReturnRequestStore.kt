package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary lease return requests by code. */
interface PrimaryLeaseReturnRequestStore {
    fun find(code: String): PrimaryLeaseReturnRequest?
    fun save(item: PrimaryLeaseReturnRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLeaseReturnRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLeaseReturnRequestStore(
    var items: MutableMap<String, PrimaryLeaseReturnRequest>,
    var order: MutableList<String>,
) : PrimaryLeaseReturnRequestStore {
    override fun find(code: String): PrimaryLeaseReturnRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLeaseReturnRequest) {
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

    override fun list(): MutableList<PrimaryLeaseReturnRequest> {
        val out = mutableListOf<PrimaryLeaseReturnRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLeaseReturnRequestStore(): PrimaryLeaseReturnRequestStore {
    return MemoryPrimaryLeaseReturnRequestStore(
        mutableMapOf<String, PrimaryLeaseReturnRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLeaseReturnRequestStore(
    var inner: PrimaryLeaseReturnRequestStore,
    var cache: TtlCache<PrimaryLeaseReturnRequest>,
    var hits: Int,
    var misses: Int,
) : PrimaryLeaseReturnRequestStore {
    override fun find(code: String): PrimaryLeaseReturnRequest? {
        val hit = (cache.get(code) as PrimaryLeaseReturnRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLeaseReturnRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLeaseReturnRequest> = inner.list()
}

fun newCachedPrimaryLeaseReturnRequestStore(inner: PrimaryLeaseReturnRequestStore, ttl: Int, clock: Clock): PrimaryLeaseReturnRequestStore {
    return CachedPrimaryLeaseReturnRequestStore(
        inner = inner,
        cache = newTtlCache<PrimaryLeaseReturnRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary lease return requests. Empty fields match all. */
data class PrimaryLeaseReturnRequestQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLeaseReturnRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryLeaseReturnRequest>): MutableList<PrimaryLeaseReturnRequest> {
        val matched = filter(items) { candidate: PrimaryLeaseReturnRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLeaseReturnRequests(store: PrimaryLeaseReturnRequestStore, query: PrimaryLeaseReturnRequestQuery): MutableList<PrimaryLeaseReturnRequest> {
    return query.apply(store.list())
}
