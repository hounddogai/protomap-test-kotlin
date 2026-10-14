package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling lease return summaries by code. */
interface RollingLeaseReturnSummaryStore {
    fun find(code: String): RollingLeaseReturnSummary?
    fun save(item: RollingLeaseReturnSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLeaseReturnSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLeaseReturnSummaryStore(
    var items: MutableMap<String, RollingLeaseReturnSummary>,
    var order: MutableList<String>,
) : RollingLeaseReturnSummaryStore {
    override fun find(code: String): RollingLeaseReturnSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLeaseReturnSummary) {
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

    override fun list(): MutableList<RollingLeaseReturnSummary> {
        val out = mutableListOf<RollingLeaseReturnSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLeaseReturnSummaryStore(): RollingLeaseReturnSummaryStore {
    return MemoryRollingLeaseReturnSummaryStore(
        mutableMapOf<String, RollingLeaseReturnSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLeaseReturnSummaryStore(
    var inner: RollingLeaseReturnSummaryStore,
    var cache: TtlCache<RollingLeaseReturnSummary>,
    var hits: Int,
    var misses: Int,
) : RollingLeaseReturnSummaryStore {
    override fun find(code: String): RollingLeaseReturnSummary? {
        val hit = (cache.get(code) as RollingLeaseReturnSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLeaseReturnSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLeaseReturnSummary> = inner.list()
}

fun newCachedRollingLeaseReturnSummaryStore(inner: RollingLeaseReturnSummaryStore, ttl: Int, clock: Clock): RollingLeaseReturnSummaryStore {
    return CachedRollingLeaseReturnSummaryStore(
        inner = inner,
        cache = newTtlCache<RollingLeaseReturnSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lease return summaries. Empty fields match all. */
data class RollingLeaseReturnSummaryQuery(
    var source: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLeaseReturnSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingLeaseReturnSummary>): MutableList<RollingLeaseReturnSummary> {
        val matched = filter(items) { candidate: RollingLeaseReturnSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLeaseReturnSummaries(store: RollingLeaseReturnSummaryStore, query: RollingLeaseReturnSummaryQuery): MutableList<RollingLeaseReturnSummary> {
    return query.apply(store.list())
}
