package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global dispute requests by code. */
interface GlobalDisputeRequestStore {
    fun find(code: String): GlobalDisputeRequest?
    fun save(item: GlobalDisputeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDisputeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDisputeRequestStore(
    var items: MutableMap<String, GlobalDisputeRequest>,
    var order: MutableList<String>,
) : GlobalDisputeRequestStore {
    override fun find(code: String): GlobalDisputeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDisputeRequest) {
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

    override fun list(): MutableList<GlobalDisputeRequest> {
        val out = mutableListOf<GlobalDisputeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDisputeRequestStore(): GlobalDisputeRequestStore {
    return MemoryGlobalDisputeRequestStore(
        mutableMapOf<String, GlobalDisputeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDisputeRequestStore(
    var inner: GlobalDisputeRequestStore,
    var cache: TtlCache<GlobalDisputeRequest>,
    var hits: Int,
    var misses: Int,
) : GlobalDisputeRequestStore {
    override fun find(code: String): GlobalDisputeRequest? {
        val hit = (cache.get(code) as GlobalDisputeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDisputeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDisputeRequest> = inner.list()
}

fun newCachedGlobalDisputeRequestStore(inner: GlobalDisputeRequestStore, ttl: Int, clock: Clock): GlobalDisputeRequestStore {
    return CachedGlobalDisputeRequestStore(
        inner = inner,
        cache = newTtlCache<GlobalDisputeRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global dispute requests. Empty fields match all. */
data class GlobalDisputeRequestQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDisputeRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<GlobalDisputeRequest>): MutableList<GlobalDisputeRequest> {
        val matched = filter(items) { candidate: GlobalDisputeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDisputeRequests(store: GlobalDisputeRequestStore, query: GlobalDisputeRequestQuery): MutableList<GlobalDisputeRequest> {
    return query.apply(store.list())
}
