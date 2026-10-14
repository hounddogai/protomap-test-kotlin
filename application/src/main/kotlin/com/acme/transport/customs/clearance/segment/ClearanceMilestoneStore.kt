package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance milestones by code. */
interface ClearanceMilestoneStore {
    fun find(code: String): ClearanceMilestone?
    fun save(item: ClearanceMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceMilestoneStore(
    var items: MutableMap<String, ClearanceMilestone>,
    var order: MutableList<String>,
) : ClearanceMilestoneStore {
    override fun find(code: String): ClearanceMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceMilestone) {
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

    override fun list(): MutableList<ClearanceMilestone> {
        val out = mutableListOf<ClearanceMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceMilestoneStore(): ClearanceMilestoneStore {
    return MemoryClearanceMilestoneStore(mutableMapOf<String, ClearanceMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceMilestoneStore(
    var inner: ClearanceMilestoneStore,
    var cache: TtlCache<ClearanceMilestone>,
    var hits: Int,
    var misses: Int,
) : ClearanceMilestoneStore {
    override fun find(code: String): ClearanceMilestone? {
        val hit = (cache.get(code) as ClearanceMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceMilestone> = inner.list()
}

fun newCachedClearanceMilestoneStore(inner: ClearanceMilestoneStore, ttl: Int, clock: Clock): ClearanceMilestoneStore {
    return CachedClearanceMilestoneStore(
        inner = inner,
        cache = newTtlCache<ClearanceMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance milestones. Empty fields match all. */
data class ClearanceMilestoneQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceMilestone>): MutableList<ClearanceMilestone> {
        val matched = filter(items) { candidate: ClearanceMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceMilestones(store: ClearanceMilestoneStore, query: ClearanceMilestoneQuery): MutableList<ClearanceMilestone> {
    return query.apply(store.list())
}
