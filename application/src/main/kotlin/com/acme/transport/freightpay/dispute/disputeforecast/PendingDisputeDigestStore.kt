package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending dispute digests by code. */
interface PendingDisputeDigestStore {
    fun find(code: String): PendingDisputeDigest?
    fun save(item: PendingDisputeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDisputeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDisputeDigestStore(
    var items: MutableMap<String, PendingDisputeDigest>,
    var order: MutableList<String>,
) : PendingDisputeDigestStore {
    override fun find(code: String): PendingDisputeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDisputeDigest) {
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

    override fun list(): MutableList<PendingDisputeDigest> {
        val out = mutableListOf<PendingDisputeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDisputeDigestStore(): PendingDisputeDigestStore {
    return MemoryPendingDisputeDigestStore(
        mutableMapOf<String, PendingDisputeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDisputeDigestStore(
    var inner: PendingDisputeDigestStore,
    var cache: TtlCache<PendingDisputeDigest>,
    var hits: Int,
    var misses: Int,
) : PendingDisputeDigestStore {
    override fun find(code: String): PendingDisputeDigest? {
        val hit = (cache.get(code) as PendingDisputeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDisputeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDisputeDigest> = inner.list()
}

fun newCachedPendingDisputeDigestStore(inner: PendingDisputeDigestStore, ttl: Int, clock: Clock): PendingDisputeDigestStore {
    return CachedPendingDisputeDigestStore(
        inner = inner,
        cache = newTtlCache<PendingDisputeDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dispute digests. Empty fields match all. */
data class PendingDisputeDigestQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDisputeDigest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<PendingDisputeDigest>): MutableList<PendingDisputeDigest> {
        val matched = filter(items) { candidate: PendingDisputeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDisputeDigests(store: PendingDisputeDigestStore, query: PendingDisputeDigestQuery): MutableList<PendingDisputeDigest> {
    return query.apply(store.list())
}
