package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default carbon report milestones by code. */
interface DefaultCarbonReportMilestoneStore {
    fun find(code: String): DefaultCarbonReportMilestone?
    fun save(item: DefaultCarbonReportMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCarbonReportMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCarbonReportMilestoneStore(
    var items: MutableMap<String, DefaultCarbonReportMilestone>,
    var order: MutableList<String>,
) : DefaultCarbonReportMilestoneStore {
    override fun find(code: String): DefaultCarbonReportMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCarbonReportMilestone) {
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

    override fun list(): MutableList<DefaultCarbonReportMilestone> {
        val out = mutableListOf<DefaultCarbonReportMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCarbonReportMilestoneStore(): DefaultCarbonReportMilestoneStore {
    return MemoryDefaultCarbonReportMilestoneStore(
        mutableMapOf<String, DefaultCarbonReportMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCarbonReportMilestoneStore(
    var inner: DefaultCarbonReportMilestoneStore,
    var cache: TtlCache<DefaultCarbonReportMilestone>,
    var hits: Int,
    var misses: Int,
) : DefaultCarbonReportMilestoneStore {
    override fun find(code: String): DefaultCarbonReportMilestone? {
        val hit = (cache.get(code) as DefaultCarbonReportMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCarbonReportMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCarbonReportMilestone> = inner.list()
}

fun newCachedDefaultCarbonReportMilestoneStore(inner: DefaultCarbonReportMilestoneStore, ttl: Int, clock: Clock): DefaultCarbonReportMilestoneStore {
    return CachedDefaultCarbonReportMilestoneStore(
        inner = inner,
        cache = newTtlCache<DefaultCarbonReportMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default carbon report milestones. Empty fields match all. */
data class DefaultCarbonReportMilestoneQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCarbonReportMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DefaultCarbonReportMilestone>): MutableList<DefaultCarbonReportMilestone> {
        val matched = filter(items) { candidate: DefaultCarbonReportMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCarbonReportMilestones(store: DefaultCarbonReportMilestoneStore, query: DefaultCarbonReportMilestoneQuery): MutableList<DefaultCarbonReportMilestone> {
    return query.apply(store.list())
}
