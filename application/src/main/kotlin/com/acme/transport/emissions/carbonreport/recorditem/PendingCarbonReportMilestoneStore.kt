package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending carbon report milestones by code. */
interface PendingCarbonReportMilestoneStore {
    fun find(code: String): PendingCarbonReportMilestone?
    fun save(item: PendingCarbonReportMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCarbonReportMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCarbonReportMilestoneStore(
    var items: MutableMap<String, PendingCarbonReportMilestone>,
    var order: MutableList<String>,
) : PendingCarbonReportMilestoneStore {
    override fun find(code: String): PendingCarbonReportMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCarbonReportMilestone) {
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

    override fun list(): MutableList<PendingCarbonReportMilestone> {
        val out = mutableListOf<PendingCarbonReportMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCarbonReportMilestoneStore(): PendingCarbonReportMilestoneStore {
    return MemoryPendingCarbonReportMilestoneStore(
        mutableMapOf<String, PendingCarbonReportMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCarbonReportMilestoneStore(
    var inner: PendingCarbonReportMilestoneStore,
    var cache: TtlCache<PendingCarbonReportMilestone>,
    var hits: Int,
    var misses: Int,
) : PendingCarbonReportMilestoneStore {
    override fun find(code: String): PendingCarbonReportMilestone? {
        val hit = (cache.get(code) as PendingCarbonReportMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCarbonReportMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCarbonReportMilestone> = inner.list()
}

fun newCachedPendingCarbonReportMilestoneStore(inner: PendingCarbonReportMilestoneStore, ttl: Int, clock: Clock): PendingCarbonReportMilestoneStore {
    return CachedPendingCarbonReportMilestoneStore(
        inner = inner,
        cache = newTtlCache<PendingCarbonReportMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending carbon report milestones. Empty fields match all. */
data class PendingCarbonReportMilestoneQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCarbonReportMilestone): Boolean {
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

    fun apply(items: MutableList<PendingCarbonReportMilestone>): MutableList<PendingCarbonReportMilestone> {
        val matched = filter(items) { candidate: PendingCarbonReportMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCarbonReportMilestones(store: PendingCarbonReportMilestoneStore, query: PendingCarbonReportMilestoneQuery): MutableList<PendingCarbonReportMilestone> {
    return query.apply(store.list())
}
