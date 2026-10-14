package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling clearance milestones by code. */
interface RollingClearanceMilestoneStore {
    fun find(code: String): RollingClearanceMilestone?
    fun save(item: RollingClearanceMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingClearanceMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingClearanceMilestoneStore(
    var items: MutableMap<String, RollingClearanceMilestone>,
    var order: MutableList<String>,
) : RollingClearanceMilestoneStore {
    override fun find(code: String): RollingClearanceMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingClearanceMilestone) {
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

    override fun list(): MutableList<RollingClearanceMilestone> {
        val out = mutableListOf<RollingClearanceMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingClearanceMilestoneStore(): RollingClearanceMilestoneStore {
    return MemoryRollingClearanceMilestoneStore(
        mutableMapOf<String, RollingClearanceMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingClearanceMilestoneStore(
    var inner: RollingClearanceMilestoneStore,
    var cache: TtlCache<RollingClearanceMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingClearanceMilestoneStore {
    override fun find(code: String): RollingClearanceMilestone? {
        val hit = (cache.get(code) as RollingClearanceMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingClearanceMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingClearanceMilestone> = inner.list()
}

fun newCachedRollingClearanceMilestoneStore(inner: RollingClearanceMilestoneStore, ttl: Int, clock: Clock): RollingClearanceMilestoneStore {
    return CachedRollingClearanceMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingClearanceMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling clearance milestones. Empty fields match all. */
data class RollingClearanceMilestoneQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingClearanceMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingClearanceMilestone>): MutableList<RollingClearanceMilestone> {
        val matched = filter(items) { candidate: RollingClearanceMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingClearanceMilestones(store: RollingClearanceMilestoneStore, query: RollingClearanceMilestoneQuery): MutableList<RollingClearanceMilestone> {
    return query.apply(store.list())
}
