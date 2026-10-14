package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient service plan summaries by code. */
interface LenientServicePlanSummaryStore {
    fun find(code: String): LenientServicePlanSummary?
    fun save(item: LenientServicePlanSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServicePlanSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServicePlanSummaryStore(
    var items: MutableMap<String, LenientServicePlanSummary>,
    var order: MutableList<String>,
) : LenientServicePlanSummaryStore {
    override fun find(code: String): LenientServicePlanSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServicePlanSummary) {
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

    override fun list(): MutableList<LenientServicePlanSummary> {
        val out = mutableListOf<LenientServicePlanSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServicePlanSummaryStore(): LenientServicePlanSummaryStore {
    return MemoryLenientServicePlanSummaryStore(
        mutableMapOf<String, LenientServicePlanSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServicePlanSummaryStore(
    var inner: LenientServicePlanSummaryStore,
    var cache: TtlCache<LenientServicePlanSummary>,
    var hits: Int,
    var misses: Int,
) : LenientServicePlanSummaryStore {
    override fun find(code: String): LenientServicePlanSummary? {
        val hit = (cache.get(code) as LenientServicePlanSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServicePlanSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServicePlanSummary> = inner.list()
}

fun newCachedLenientServicePlanSummaryStore(inner: LenientServicePlanSummaryStore, ttl: Int, clock: Clock): LenientServicePlanSummaryStore {
    return CachedLenientServicePlanSummaryStore(
        inner = inner,
        cache = newTtlCache<LenientServicePlanSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service plan summaries. Empty fields match all. */
data class LenientServicePlanSummaryQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServicePlanSummary): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServicePlanSummary>): MutableList<LenientServicePlanSummary> {
        val matched = filter(items) { candidate: LenientServicePlanSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServicePlanSummaries(store: LenientServicePlanSummaryStore, query: LenientServicePlanSummaryQuery): MutableList<LenientServicePlanSummary> {
    return query.apply(store.list())
}
