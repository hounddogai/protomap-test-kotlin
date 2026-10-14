package com.acme.transport.customs.inspection.window

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling inspection milestones by code. */
interface RollingInspectionMilestoneStore {
    fun find(code: String): RollingInspectionMilestone?
    fun save(item: RollingInspectionMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingInspectionMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingInspectionMilestoneStore(
    var items: MutableMap<String, RollingInspectionMilestone>,
    var order: MutableList<String>,
) : RollingInspectionMilestoneStore {
    override fun find(code: String): RollingInspectionMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingInspectionMilestone) {
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

    override fun list(): MutableList<RollingInspectionMilestone> {
        val out = mutableListOf<RollingInspectionMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingInspectionMilestoneStore(): RollingInspectionMilestoneStore {
    return MemoryRollingInspectionMilestoneStore(
        mutableMapOf<String, RollingInspectionMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingInspectionMilestoneStore(
    var inner: RollingInspectionMilestoneStore,
    var cache: TtlCache<RollingInspectionMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingInspectionMilestoneStore {
    override fun find(code: String): RollingInspectionMilestone? {
        val hit = (cache.get(code) as RollingInspectionMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingInspectionMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingInspectionMilestone> = inner.list()
}

fun newCachedRollingInspectionMilestoneStore(inner: RollingInspectionMilestoneStore, ttl: Int, clock: Clock): RollingInspectionMilestoneStore {
    return CachedRollingInspectionMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingInspectionMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling inspection milestones. Empty fields match all. */
data class RollingInspectionMilestoneQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingInspectionMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingInspectionMilestone>): MutableList<RollingInspectionMilestone> {
        val matched = filter(items) { candidate: RollingInspectionMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingInspectionMilestones(store: RollingInspectionMilestoneStore, query: RollingInspectionMilestoneQuery): MutableList<RollingInspectionMilestone> {
    return query.apply(store.list())
}
