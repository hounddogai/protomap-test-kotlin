package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling lease return requests by code. */
interface RollingLeaseReturnRequestStore {
    fun find(code: String): RollingLeaseReturnRequest?
    fun save(item: RollingLeaseReturnRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLeaseReturnRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLeaseReturnRequestStore(
    var items: MutableMap<String, RollingLeaseReturnRequest>,
    var order: MutableList<String>,
) : RollingLeaseReturnRequestStore {
    override fun find(code: String): RollingLeaseReturnRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLeaseReturnRequest) {
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

    override fun list(): MutableList<RollingLeaseReturnRequest> {
        val out = mutableListOf<RollingLeaseReturnRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLeaseReturnRequestStore(): RollingLeaseReturnRequestStore {
    return MemoryRollingLeaseReturnRequestStore(
        mutableMapOf<String, RollingLeaseReturnRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLeaseReturnRequestStore(
    var inner: RollingLeaseReturnRequestStore,
    var cache: TtlCache<RollingLeaseReturnRequest>,
    var hits: Int,
    var misses: Int,
) : RollingLeaseReturnRequestStore {
    override fun find(code: String): RollingLeaseReturnRequest? {
        val hit = (cache.get(code) as RollingLeaseReturnRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLeaseReturnRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLeaseReturnRequest> = inner.list()
}

fun newCachedRollingLeaseReturnRequestStore(inner: RollingLeaseReturnRequestStore, ttl: Int, clock: Clock): RollingLeaseReturnRequestStore {
    return CachedRollingLeaseReturnRequestStore(
        inner = inner,
        cache = newTtlCache<RollingLeaseReturnRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lease return requests. Empty fields match all. */
data class RollingLeaseReturnRequestQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLeaseReturnRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingLeaseReturnRequest>): MutableList<RollingLeaseReturnRequest> {
        val matched = filter(items) { candidate: RollingLeaseReturnRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLeaseReturnRequests(store: RollingLeaseReturnRequestStore, query: RollingLeaseReturnRequestQuery): MutableList<RollingLeaseReturnRequest> {
    return query.apply(store.list())
}
