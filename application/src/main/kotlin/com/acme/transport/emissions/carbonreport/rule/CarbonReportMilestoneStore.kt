package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report milestones by code. */
interface CarbonReportMilestoneStore {
    fun find(code: String): CarbonReportMilestone?
    fun save(item: CarbonReportMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportMilestoneStore(
    var items: MutableMap<String, CarbonReportMilestone>,
    var order: MutableList<String>,
) : CarbonReportMilestoneStore {
    override fun find(code: String): CarbonReportMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportMilestone) {
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

    override fun list(): MutableList<CarbonReportMilestone> {
        val out = mutableListOf<CarbonReportMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportMilestoneStore(): CarbonReportMilestoneStore {
    return MemoryCarbonReportMilestoneStore(
        mutableMapOf<String, CarbonReportMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportMilestoneStore(
    var inner: CarbonReportMilestoneStore,
    var cache: TtlCache<CarbonReportMilestone>,
    var hits: Int,
    var misses: Int,
) : CarbonReportMilestoneStore {
    override fun find(code: String): CarbonReportMilestone? {
        val hit = (cache.get(code) as CarbonReportMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportMilestone> = inner.list()
}

fun newCachedCarbonReportMilestoneStore(inner: CarbonReportMilestoneStore, ttl: Int, clock: Clock): CarbonReportMilestoneStore {
    return CachedCarbonReportMilestoneStore(
        inner = inner,
        cache = newTtlCache<CarbonReportMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report milestones. Empty fields match all. */
data class CarbonReportMilestoneQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportMilestone): Boolean {
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

    fun apply(items: MutableList<CarbonReportMilestone>): MutableList<CarbonReportMilestone> {
        val matched = filter(items) { candidate: CarbonReportMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportMilestones(store: CarbonReportMilestoneStore, query: CarbonReportMilestoneQuery): MutableList<CarbonReportMilestone> {
    return query.apply(store.list())
}
