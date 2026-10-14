package com.acme.transport.safety.coaching.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching milestones by code. */
interface CoachingMilestoneStore {
    fun find(code: String): CoachingMilestone?
    fun save(item: CoachingMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingMilestoneStore(
    var items: MutableMap<String, CoachingMilestone>,
    var order: MutableList<String>,
) : CoachingMilestoneStore {
    override fun find(code: String): CoachingMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingMilestone) {
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

    override fun list(): MutableList<CoachingMilestone> {
        val out = mutableListOf<CoachingMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingMilestoneStore(): CoachingMilestoneStore {
    return MemoryCoachingMilestoneStore(mutableMapOf<String, CoachingMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingMilestoneStore(
    var inner: CoachingMilestoneStore,
    var cache: TtlCache<CoachingMilestone>,
    var hits: Int,
    var misses: Int,
) : CoachingMilestoneStore {
    override fun find(code: String): CoachingMilestone? {
        val hit = (cache.get(code) as CoachingMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingMilestone> = inner.list()
}

fun newCachedCoachingMilestoneStore(inner: CoachingMilestoneStore, ttl: Int, clock: Clock): CoachingMilestoneStore {
    return CachedCoachingMilestoneStore(
        inner = inner,
        cache = newTtlCache<CoachingMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching milestones. Empty fields match all. */
data class CoachingMilestoneQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CoachingMilestone>): MutableList<CoachingMilestone> {
        val matched = filter(items) { candidate: CoachingMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingMilestones(store: CoachingMilestoneStore, query: CoachingMilestoneQuery): MutableList<CoachingMilestone> {
    return query.apply(store.list())
}
