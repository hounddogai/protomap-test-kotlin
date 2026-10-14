package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending dispute claims by code. */
interface PendingDisputeClaimStore {
    fun find(code: String): PendingDisputeClaim?
    fun save(item: PendingDisputeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDisputeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDisputeClaimStore(
    var items: MutableMap<String, PendingDisputeClaim>,
    var order: MutableList<String>,
) : PendingDisputeClaimStore {
    override fun find(code: String): PendingDisputeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDisputeClaim) {
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

    override fun list(): MutableList<PendingDisputeClaim> {
        val out = mutableListOf<PendingDisputeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDisputeClaimStore(): PendingDisputeClaimStore {
    return MemoryPendingDisputeClaimStore(mutableMapOf<String, PendingDisputeClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDisputeClaimStore(
    var inner: PendingDisputeClaimStore,
    var cache: TtlCache<PendingDisputeClaim>,
    var hits: Int,
    var misses: Int,
) : PendingDisputeClaimStore {
    override fun find(code: String): PendingDisputeClaim? {
        val hit = (cache.get(code) as PendingDisputeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDisputeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDisputeClaim> = inner.list()
}

fun newCachedPendingDisputeClaimStore(inner: PendingDisputeClaimStore, ttl: Int, clock: Clock): PendingDisputeClaimStore {
    return CachedPendingDisputeClaimStore(
        inner = inner,
        cache = newTtlCache<PendingDisputeClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dispute claims. Empty fields match all. */
data class PendingDisputeClaimQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDisputeClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDisputeClaim>): MutableList<PendingDisputeClaim> {
        val matched = filter(items) { candidate: PendingDisputeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDisputeClaims(store: PendingDisputeClaimStore, query: PendingDisputeClaimQuery): MutableList<PendingDisputeClaim> {
    return query.apply(store.list())
}
