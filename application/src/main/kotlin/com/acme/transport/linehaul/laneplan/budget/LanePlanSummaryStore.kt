package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan summaries by code. */
interface LanePlanSummaryStore {
    fun find(code: String): LanePlanSummary?
    fun save(item: LanePlanSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanSummaryStore(
    var items: MutableMap<String, LanePlanSummary>,
    var order: MutableList<String>,
) : LanePlanSummaryStore {
    override fun find(code: String): LanePlanSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanSummary) {
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

    override fun list(): MutableList<LanePlanSummary> {
        val out = mutableListOf<LanePlanSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanSummaryStore(): LanePlanSummaryStore {
    return MemoryLanePlanSummaryStore(mutableMapOf<String, LanePlanSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanSummaryStore(
    var inner: LanePlanSummaryStore,
    var cache: TtlCache<LanePlanSummary>,
    var hits: Int,
    var misses: Int,
) : LanePlanSummaryStore {
    override fun find(code: String): LanePlanSummary? {
        val hit = (cache.get(code) as LanePlanSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanSummary> = inner.list()
}

fun newCachedLanePlanSummaryStore(inner: LanePlanSummaryStore, ttl: Int, clock: Clock): LanePlanSummaryStore {
    return CachedLanePlanSummaryStore(
        inner = inner,
        cache = newTtlCache<LanePlanSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan summaries. Empty fields match all. */
data class LanePlanSummaryQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanSummary): Boolean {
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

    fun apply(items: MutableList<LanePlanSummary>): MutableList<LanePlanSummary> {
        val matched = filter(items) { candidate: LanePlanSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanSummaries(store: LanePlanSummaryStore, query: LanePlanSummaryQuery): MutableList<LanePlanSummary> {
    return query.apply(store.list())
}
