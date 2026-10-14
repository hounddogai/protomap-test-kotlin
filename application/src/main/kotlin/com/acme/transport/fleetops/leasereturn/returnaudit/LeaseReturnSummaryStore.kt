package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return summaries by code. */
interface LeaseReturnSummaryStore {
    fun find(code: String): LeaseReturnSummary?
    fun save(item: LeaseReturnSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnSummaryStore(
    var items: MutableMap<String, LeaseReturnSummary>,
    var order: MutableList<String>,
) : LeaseReturnSummaryStore {
    override fun find(code: String): LeaseReturnSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnSummary) {
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

    override fun list(): MutableList<LeaseReturnSummary> {
        val out = mutableListOf<LeaseReturnSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnSummaryStore(): LeaseReturnSummaryStore {
    return MemoryLeaseReturnSummaryStore(mutableMapOf<String, LeaseReturnSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnSummaryStore(
    var inner: LeaseReturnSummaryStore,
    var cache: TtlCache<LeaseReturnSummary>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnSummaryStore {
    override fun find(code: String): LeaseReturnSummary? {
        val hit = (cache.get(code) as LeaseReturnSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnSummary> = inner.list()
}

fun newCachedLeaseReturnSummaryStore(inner: LeaseReturnSummaryStore, ttl: Int, clock: Clock): LeaseReturnSummaryStore {
    return CachedLeaseReturnSummaryStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return summaries. Empty fields match all. */
data class LeaseReturnSummaryQuery(
    var region: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnSummary>): MutableList<LeaseReturnSummary> {
        val matched = filter(items) { candidate: LeaseReturnSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnSummaries(store: LeaseReturnSummaryStore, query: LeaseReturnSummaryQuery): MutableList<LeaseReturnSummary> {
    return query.apply(store.list())
}
