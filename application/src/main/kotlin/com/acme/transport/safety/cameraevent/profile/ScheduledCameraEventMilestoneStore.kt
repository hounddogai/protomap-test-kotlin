package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled camera event milestones by code. */
interface ScheduledCameraEventMilestoneStore {
    fun find(code: String): ScheduledCameraEventMilestone?
    fun save(item: ScheduledCameraEventMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCameraEventMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCameraEventMilestoneStore(
    var items: MutableMap<String, ScheduledCameraEventMilestone>,
    var order: MutableList<String>,
) : ScheduledCameraEventMilestoneStore {
    override fun find(code: String): ScheduledCameraEventMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCameraEventMilestone) {
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

    override fun list(): MutableList<ScheduledCameraEventMilestone> {
        val out = mutableListOf<ScheduledCameraEventMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCameraEventMilestoneStore(): ScheduledCameraEventMilestoneStore {
    return MemoryScheduledCameraEventMilestoneStore(
        mutableMapOf<String, ScheduledCameraEventMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCameraEventMilestoneStore(
    var inner: ScheduledCameraEventMilestoneStore,
    var cache: TtlCache<ScheduledCameraEventMilestone>,
    var hits: Int,
    var misses: Int,
) : ScheduledCameraEventMilestoneStore {
    override fun find(code: String): ScheduledCameraEventMilestone? {
        val hit = (cache.get(code) as ScheduledCameraEventMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCameraEventMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCameraEventMilestone> = inner.list()
}

fun newCachedScheduledCameraEventMilestoneStore(inner: ScheduledCameraEventMilestoneStore, ttl: Int, clock: Clock): ScheduledCameraEventMilestoneStore {
    return CachedScheduledCameraEventMilestoneStore(
        inner = inner,
        cache = newTtlCache<ScheduledCameraEventMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled camera event milestones. Empty fields match all. */
data class ScheduledCameraEventMilestoneQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCameraEventMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledCameraEventMilestone>): MutableList<ScheduledCameraEventMilestone> {
        val matched = filter(items) { candidate: ScheduledCameraEventMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCameraEventMilestones(store: ScheduledCameraEventMilestoneStore, query: ScheduledCameraEventMilestoneQuery): MutableList<ScheduledCameraEventMilestone> {
    return query.apply(store.list())
}
