package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker milestones by code. */
interface LockerMilestoneStore {
    fun find(code: String): LockerMilestone?
    fun save(item: LockerMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerMilestoneStore(
    var items: MutableMap<String, LockerMilestone>,
    var order: MutableList<String>,
) : LockerMilestoneStore {
    override fun find(code: String): LockerMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerMilestone) {
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

    override fun list(): MutableList<LockerMilestone> {
        val out = mutableListOf<LockerMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerMilestoneStore(): LockerMilestoneStore {
    return MemoryLockerMilestoneStore(mutableMapOf<String, LockerMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerMilestoneStore(
    var inner: LockerMilestoneStore,
    var cache: TtlCache<LockerMilestone>,
    var hits: Int,
    var misses: Int,
) : LockerMilestoneStore {
    override fun find(code: String): LockerMilestone? {
        val hit = (cache.get(code) as LockerMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerMilestone> = inner.list()
}

fun newCachedLockerMilestoneStore(inner: LockerMilestoneStore, ttl: Int, clock: Clock): LockerMilestoneStore {
    return CachedLockerMilestoneStore(
        inner = inner,
        cache = newTtlCache<LockerMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker milestones. Empty fields match all. */
data class LockerMilestoneQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LockerMilestone>): MutableList<LockerMilestone> {
        val matched = filter(items) { candidate: LockerMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerMilestones(store: LockerMilestoneStore, query: LockerMilestoneQuery): MutableList<LockerMilestone> {
    return query.apply(store.list())
}
