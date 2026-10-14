package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rest break milestones by code. */
interface RollingRestBreakMilestoneStore {
    fun find(code: String): RollingRestBreakMilestone?
    fun save(item: RollingRestBreakMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRestBreakMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRestBreakMilestoneStore(
    var items: MutableMap<String, RollingRestBreakMilestone>,
    var order: MutableList<String>,
) : RollingRestBreakMilestoneStore {
    override fun find(code: String): RollingRestBreakMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRestBreakMilestone) {
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

    override fun list(): MutableList<RollingRestBreakMilestone> {
        val out = mutableListOf<RollingRestBreakMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRestBreakMilestoneStore(): RollingRestBreakMilestoneStore {
    return MemoryRollingRestBreakMilestoneStore(
        mutableMapOf<String, RollingRestBreakMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRestBreakMilestoneStore(
    var inner: RollingRestBreakMilestoneStore,
    var cache: TtlCache<RollingRestBreakMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingRestBreakMilestoneStore {
    override fun find(code: String): RollingRestBreakMilestone? {
        val hit = (cache.get(code) as RollingRestBreakMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRestBreakMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRestBreakMilestone> = inner.list()
}

fun newCachedRollingRestBreakMilestoneStore(inner: RollingRestBreakMilestoneStore, ttl: Int, clock: Clock): RollingRestBreakMilestoneStore {
    return CachedRollingRestBreakMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingRestBreakMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rest break milestones. Empty fields match all. */
data class RollingRestBreakMilestoneQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRestBreakMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRestBreakMilestone>): MutableList<RollingRestBreakMilestone> {
        val matched = filter(items) { candidate: RollingRestBreakMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRestBreakMilestones(store: RollingRestBreakMilestoneStore, query: RollingRestBreakMilestoneQuery): MutableList<RollingRestBreakMilestone> {
    return query.apply(store.list())
}
