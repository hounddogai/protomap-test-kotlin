package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling dispute requests by code. */
interface RollingDisputeRequestStore {
    fun find(code: String): RollingDisputeRequest?
    fun save(item: RollingDisputeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDisputeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDisputeRequestStore(
    var items: MutableMap<String, RollingDisputeRequest>,
    var order: MutableList<String>,
) : RollingDisputeRequestStore {
    override fun find(code: String): RollingDisputeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDisputeRequest) {
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

    override fun list(): MutableList<RollingDisputeRequest> {
        val out = mutableListOf<RollingDisputeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDisputeRequestStore(): RollingDisputeRequestStore {
    return MemoryRollingDisputeRequestStore(
        mutableMapOf<String, RollingDisputeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDisputeRequestStore(
    var inner: RollingDisputeRequestStore,
    var cache: TtlCache<RollingDisputeRequest>,
    var hits: Int,
    var misses: Int,
) : RollingDisputeRequestStore {
    override fun find(code: String): RollingDisputeRequest? {
        val hit = (cache.get(code) as RollingDisputeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDisputeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDisputeRequest> = inner.list()
}

fun newCachedRollingDisputeRequestStore(inner: RollingDisputeRequestStore, ttl: Int, clock: Clock): RollingDisputeRequestStore {
    return CachedRollingDisputeRequestStore(
        inner = inner,
        cache = newTtlCache<RollingDisputeRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dispute requests. Empty fields match all. */
data class RollingDisputeRequestQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDisputeRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingDisputeRequest>): MutableList<RollingDisputeRequest> {
        val matched = filter(items) { candidate: RollingDisputeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDisputeRequests(store: RollingDisputeRequestStore, query: RollingDisputeRequestQuery): MutableList<RollingDisputeRequest> {
    return query.apply(store.list())
}
