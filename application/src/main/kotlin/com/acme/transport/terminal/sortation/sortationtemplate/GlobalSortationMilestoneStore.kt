package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global sortation milestones by code. */
interface GlobalSortationMilestoneStore {
    fun find(code: String): GlobalSortationMilestone?
    fun save(item: GlobalSortationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSortationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSortationMilestoneStore(
    var items: MutableMap<String, GlobalSortationMilestone>,
    var order: MutableList<String>,
) : GlobalSortationMilestoneStore {
    override fun find(code: String): GlobalSortationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSortationMilestone) {
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

    override fun list(): MutableList<GlobalSortationMilestone> {
        val out = mutableListOf<GlobalSortationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSortationMilestoneStore(): GlobalSortationMilestoneStore {
    return MemoryGlobalSortationMilestoneStore(
        mutableMapOf<String, GlobalSortationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSortationMilestoneStore(
    var inner: GlobalSortationMilestoneStore,
    var cache: TtlCache<GlobalSortationMilestone>,
    var hits: Int,
    var misses: Int,
) : GlobalSortationMilestoneStore {
    override fun find(code: String): GlobalSortationMilestone? {
        val hit = (cache.get(code) as GlobalSortationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSortationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSortationMilestone> = inner.list()
}

fun newCachedGlobalSortationMilestoneStore(inner: GlobalSortationMilestoneStore, ttl: Int, clock: Clock): GlobalSortationMilestoneStore {
    return CachedGlobalSortationMilestoneStore(
        inner = inner,
        cache = newTtlCache<GlobalSortationMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global sortation milestones. Empty fields match all. */
data class GlobalSortationMilestoneQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSortationMilestone): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalSortationMilestone>): MutableList<GlobalSortationMilestone> {
        val matched = filter(items) { candidate: GlobalSortationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSortationMilestones(store: GlobalSortationMilestoneStore, query: GlobalSortationMilestoneQuery): MutableList<GlobalSortationMilestone> {
    return query.apply(store.list())
}
