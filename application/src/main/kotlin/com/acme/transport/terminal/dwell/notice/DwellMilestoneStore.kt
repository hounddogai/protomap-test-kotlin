package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell milestones by code. */
interface DwellMilestoneStore {
    fun find(code: String): DwellMilestone?
    fun save(item: DwellMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellMilestoneStore(
    var items: MutableMap<String, DwellMilestone>,
    var order: MutableList<String>,
) : DwellMilestoneStore {
    override fun find(code: String): DwellMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellMilestone) {
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

    override fun list(): MutableList<DwellMilestone> {
        val out = mutableListOf<DwellMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellMilestoneStore(): DwellMilestoneStore {
    return MemoryDwellMilestoneStore(mutableMapOf<String, DwellMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellMilestoneStore(
    var inner: DwellMilestoneStore,
    var cache: TtlCache<DwellMilestone>,
    var hits: Int,
    var misses: Int,
) : DwellMilestoneStore {
    override fun find(code: String): DwellMilestone? {
        val hit = (cache.get(code) as DwellMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellMilestone> = inner.list()
}

fun newCachedDwellMilestoneStore(inner: DwellMilestoneStore, ttl: Int, clock: Clock): DwellMilestoneStore {
    return CachedDwellMilestoneStore(
        inner = inner,
        cache = newTtlCache<DwellMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell milestones. Empty fields match all. */
data class DwellMilestoneQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellMilestone): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellMilestone>): MutableList<DwellMilestone> {
        val matched = filter(items) { candidate: DwellMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellMilestones(store: DwellMilestoneStore, query: DwellMilestoneQuery): MutableList<DwellMilestone> {
    return query.apply(store.list())
}
