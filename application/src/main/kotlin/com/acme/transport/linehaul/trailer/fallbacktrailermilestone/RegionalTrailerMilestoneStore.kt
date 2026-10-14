package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional trailer milestones by code. */
interface RegionalTrailerMilestoneStore {
    fun find(code: String): RegionalTrailerMilestone?
    fun save(item: RegionalTrailerMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerMilestoneStore(
    var items: MutableMap<String, RegionalTrailerMilestone>,
    var order: MutableList<String>,
) : RegionalTrailerMilestoneStore {
    override fun find(code: String): RegionalTrailerMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerMilestone) {
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

    override fun list(): MutableList<RegionalTrailerMilestone> {
        val out = mutableListOf<RegionalTrailerMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerMilestoneStore(): RegionalTrailerMilestoneStore {
    return MemoryRegionalTrailerMilestoneStore(
        mutableMapOf<String, RegionalTrailerMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerMilestoneStore(
    var inner: RegionalTrailerMilestoneStore,
    var cache: TtlCache<RegionalTrailerMilestone>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerMilestoneStore {
    override fun find(code: String): RegionalTrailerMilestone? {
        val hit = (cache.get(code) as RegionalTrailerMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerMilestone> = inner.list()
}

fun newCachedRegionalTrailerMilestoneStore(inner: RegionalTrailerMilestoneStore, ttl: Int, clock: Clock): RegionalTrailerMilestoneStore {
    return CachedRegionalTrailerMilestoneStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer milestones. Empty fields match all. */
data class RegionalTrailerMilestoneQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerMilestone>): MutableList<RegionalTrailerMilestone> {
        val matched = filter(items) { candidate: RegionalTrailerMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerMilestones(store: RegionalTrailerMilestoneStore, query: RegionalTrailerMilestoneQuery): MutableList<RegionalTrailerMilestone> {
    return query.apply(store.list())
}
