package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual dispute requests by code. */
interface ManualDisputeRequestStore {
    fun find(code: String): ManualDisputeRequest?
    fun save(item: ManualDisputeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDisputeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDisputeRequestStore(
    var items: MutableMap<String, ManualDisputeRequest>,
    var order: MutableList<String>,
) : ManualDisputeRequestStore {
    override fun find(code: String): ManualDisputeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDisputeRequest) {
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

    override fun list(): MutableList<ManualDisputeRequest> {
        val out = mutableListOf<ManualDisputeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDisputeRequestStore(): ManualDisputeRequestStore {
    return MemoryManualDisputeRequestStore(
        mutableMapOf<String, ManualDisputeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDisputeRequestStore(
    var inner: ManualDisputeRequestStore,
    var cache: TtlCache<ManualDisputeRequest>,
    var hits: Int,
    var misses: Int,
) : ManualDisputeRequestStore {
    override fun find(code: String): ManualDisputeRequest? {
        val hit = (cache.get(code) as ManualDisputeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDisputeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDisputeRequest> = inner.list()
}

fun newCachedManualDisputeRequestStore(inner: ManualDisputeRequestStore, ttl: Int, clock: Clock): ManualDisputeRequestStore {
    return CachedManualDisputeRequestStore(
        inner = inner,
        cache = newTtlCache<ManualDisputeRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dispute requests. Empty fields match all. */
data class ManualDisputeRequestQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDisputeRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDisputeRequest>): MutableList<ManualDisputeRequest> {
        val matched = filter(items) { candidate: ManualDisputeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDisputeRequests(store: ManualDisputeRequestStore, query: ManualDisputeRequestQuery): MutableList<ManualDisputeRequest> {
    return query.apply(store.list())
}
