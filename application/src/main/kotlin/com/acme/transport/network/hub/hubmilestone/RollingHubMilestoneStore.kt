package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling hub milestones by code. */
interface RollingHubMilestoneStore {
    fun find(code: String): RollingHubMilestone?
    fun save(item: RollingHubMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingHubMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingHubMilestoneStore(
    var items: MutableMap<String, RollingHubMilestone>,
    var order: MutableList<String>,
) : RollingHubMilestoneStore {
    override fun find(code: String): RollingHubMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingHubMilestone) {
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

    override fun list(): MutableList<RollingHubMilestone> {
        val out = mutableListOf<RollingHubMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingHubMilestoneStore(): RollingHubMilestoneStore {
    return MemoryRollingHubMilestoneStore(mutableMapOf<String, RollingHubMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingHubMilestoneStore(
    var inner: RollingHubMilestoneStore,
    var cache: TtlCache<RollingHubMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingHubMilestoneStore {
    override fun find(code: String): RollingHubMilestone? {
        val hit = (cache.get(code) as RollingHubMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingHubMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingHubMilestone> = inner.list()
}

fun newCachedRollingHubMilestoneStore(inner: RollingHubMilestoneStore, ttl: Int, clock: Clock): RollingHubMilestoneStore {
    return CachedRollingHubMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingHubMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling hub milestones. Empty fields match all. */
data class RollingHubMilestoneQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingHubMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingHubMilestone>): MutableList<RollingHubMilestone> {
        val matched = filter(items) { candidate: RollingHubMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingHubMilestones(store: RollingHubMilestoneStore, query: RollingHubMilestoneQuery): MutableList<RollingHubMilestone> {
    return query.apply(store.list())
}
