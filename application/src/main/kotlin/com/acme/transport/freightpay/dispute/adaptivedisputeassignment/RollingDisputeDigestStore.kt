package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling dispute digests by code. */
interface RollingDisputeDigestStore {
    fun find(code: String): RollingDisputeDigest?
    fun save(item: RollingDisputeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDisputeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDisputeDigestStore(
    var items: MutableMap<String, RollingDisputeDigest>,
    var order: MutableList<String>,
) : RollingDisputeDigestStore {
    override fun find(code: String): RollingDisputeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDisputeDigest) {
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

    override fun list(): MutableList<RollingDisputeDigest> {
        val out = mutableListOf<RollingDisputeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDisputeDigestStore(): RollingDisputeDigestStore {
    return MemoryRollingDisputeDigestStore(
        mutableMapOf<String, RollingDisputeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDisputeDigestStore(
    var inner: RollingDisputeDigestStore,
    var cache: TtlCache<RollingDisputeDigest>,
    var hits: Int,
    var misses: Int,
) : RollingDisputeDigestStore {
    override fun find(code: String): RollingDisputeDigest? {
        val hit = (cache.get(code) as RollingDisputeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDisputeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDisputeDigest> = inner.list()
}

fun newCachedRollingDisputeDigestStore(inner: RollingDisputeDigestStore, ttl: Int, clock: Clock): RollingDisputeDigestStore {
    return CachedRollingDisputeDigestStore(
        inner = inner,
        cache = newTtlCache<RollingDisputeDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dispute digests. Empty fields match all. */
data class RollingDisputeDigestQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDisputeDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDisputeDigest>): MutableList<RollingDisputeDigest> {
        val matched = filter(items) { candidate: RollingDisputeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDisputeDigests(store: RollingDisputeDigestStore, query: RollingDisputeDigestQuery): MutableList<RollingDisputeDigest> {
    return query.apply(store.list())
}
