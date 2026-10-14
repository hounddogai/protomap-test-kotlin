package com.acme.transport.freightpay.dispute.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending dispute baselines by code. */
interface PendingDisputeBaselineStore {
    fun find(code: String): PendingDisputeBaseline?
    fun save(item: PendingDisputeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDisputeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDisputeBaselineStore(
    var items: MutableMap<String, PendingDisputeBaseline>,
    var order: MutableList<String>,
) : PendingDisputeBaselineStore {
    override fun find(code: String): PendingDisputeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDisputeBaseline) {
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

    override fun list(): MutableList<PendingDisputeBaseline> {
        val out = mutableListOf<PendingDisputeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDisputeBaselineStore(): PendingDisputeBaselineStore {
    return MemoryPendingDisputeBaselineStore(
        mutableMapOf<String, PendingDisputeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDisputeBaselineStore(
    var inner: PendingDisputeBaselineStore,
    var cache: TtlCache<PendingDisputeBaseline>,
    var hits: Int,
    var misses: Int,
) : PendingDisputeBaselineStore {
    override fun find(code: String): PendingDisputeBaseline? {
        val hit = (cache.get(code) as PendingDisputeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDisputeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDisputeBaseline> = inner.list()
}

fun newCachedPendingDisputeBaselineStore(inner: PendingDisputeBaselineStore, ttl: Int, clock: Clock): PendingDisputeBaselineStore {
    return CachedPendingDisputeBaselineStore(
        inner = inner,
        cache = newTtlCache<PendingDisputeBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dispute baselines. Empty fields match all. */
data class PendingDisputeBaselineQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDisputeBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDisputeBaseline>): MutableList<PendingDisputeBaseline> {
        val matched = filter(items) { candidate: PendingDisputeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDisputeBaselines(store: PendingDisputeBaselineStore, query: PendingDisputeBaselineQuery): MutableList<PendingDisputeBaseline> {
    return query.apply(store.list())
}
