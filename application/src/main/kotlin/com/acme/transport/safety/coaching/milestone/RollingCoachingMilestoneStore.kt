package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling coaching milestones by code. */
interface RollingCoachingMilestoneStore {
    fun find(code: String): RollingCoachingMilestone?
    fun save(item: RollingCoachingMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCoachingMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCoachingMilestoneStore(
    var items: MutableMap<String, RollingCoachingMilestone>,
    var order: MutableList<String>,
) : RollingCoachingMilestoneStore {
    override fun find(code: String): RollingCoachingMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCoachingMilestone) {
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

    override fun list(): MutableList<RollingCoachingMilestone> {
        val out = mutableListOf<RollingCoachingMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCoachingMilestoneStore(): RollingCoachingMilestoneStore {
    return MemoryRollingCoachingMilestoneStore(
        mutableMapOf<String, RollingCoachingMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCoachingMilestoneStore(
    var inner: RollingCoachingMilestoneStore,
    var cache: TtlCache<RollingCoachingMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingCoachingMilestoneStore {
    override fun find(code: String): RollingCoachingMilestone? {
        val hit = (cache.get(code) as RollingCoachingMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCoachingMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCoachingMilestone> = inner.list()
}

fun newCachedRollingCoachingMilestoneStore(inner: RollingCoachingMilestoneStore, ttl: Int, clock: Clock): RollingCoachingMilestoneStore {
    return CachedRollingCoachingMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingCoachingMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling coaching milestones. Empty fields match all. */
data class RollingCoachingMilestoneQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCoachingMilestone): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCoachingMilestone>): MutableList<RollingCoachingMilestone> {
        val matched = filter(items) { candidate: RollingCoachingMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCoachingMilestones(store: RollingCoachingMilestoneStore, query: RollingCoachingMilestoneQuery): MutableList<RollingCoachingMilestone> {
    return query.apply(store.list())
}
