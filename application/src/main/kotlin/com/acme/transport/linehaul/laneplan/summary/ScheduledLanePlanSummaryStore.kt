package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled lane plan summaries by code. */
interface ScheduledLanePlanSummaryStore {
    fun find(code: String): ScheduledLanePlanSummary?
    fun save(item: ScheduledLanePlanSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLanePlanSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLanePlanSummaryStore(
    var items: MutableMap<String, ScheduledLanePlanSummary>,
    var order: MutableList<String>,
) : ScheduledLanePlanSummaryStore {
    override fun find(code: String): ScheduledLanePlanSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLanePlanSummary) {
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

    override fun list(): MutableList<ScheduledLanePlanSummary> {
        val out = mutableListOf<ScheduledLanePlanSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLanePlanSummaryStore(): ScheduledLanePlanSummaryStore {
    return MemoryScheduledLanePlanSummaryStore(
        mutableMapOf<String, ScheduledLanePlanSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLanePlanSummaryStore(
    var inner: ScheduledLanePlanSummaryStore,
    var cache: TtlCache<ScheduledLanePlanSummary>,
    var hits: Int,
    var misses: Int,
) : ScheduledLanePlanSummaryStore {
    override fun find(code: String): ScheduledLanePlanSummary? {
        val hit = (cache.get(code) as ScheduledLanePlanSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLanePlanSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLanePlanSummary> = inner.list()
}

fun newCachedScheduledLanePlanSummaryStore(inner: ScheduledLanePlanSummaryStore, ttl: Int, clock: Clock): ScheduledLanePlanSummaryStore {
    return CachedScheduledLanePlanSummaryStore(
        inner = inner,
        cache = newTtlCache<ScheduledLanePlanSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lane plan summaries. Empty fields match all. */
data class ScheduledLanePlanSummaryQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLanePlanSummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLanePlanSummary>): MutableList<ScheduledLanePlanSummary> {
        val matched = filter(items) { candidate: ScheduledLanePlanSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLanePlanSummaries(store: ScheduledLanePlanSummaryStore, query: ScheduledLanePlanSummaryQuery): MutableList<ScheduledLanePlanSummary> {
    return query.apply(store.list())
}
