package com.acme.transport.customs.clearance.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global clearance milestones by code. */
interface GlobalClearanceMilestoneStore {
    fun find(code: String): GlobalClearanceMilestone?
    fun save(item: GlobalClearanceMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalClearanceMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalClearanceMilestoneStore(
    var items: MutableMap<String, GlobalClearanceMilestone>,
    var order: MutableList<String>,
) : GlobalClearanceMilestoneStore {
    override fun find(code: String): GlobalClearanceMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalClearanceMilestone) {
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

    override fun list(): MutableList<GlobalClearanceMilestone> {
        val out = mutableListOf<GlobalClearanceMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalClearanceMilestoneStore(): GlobalClearanceMilestoneStore {
    return MemoryGlobalClearanceMilestoneStore(
        mutableMapOf<String, GlobalClearanceMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalClearanceMilestoneStore(
    var inner: GlobalClearanceMilestoneStore,
    var cache: TtlCache<GlobalClearanceMilestone>,
    var hits: Int,
    var misses: Int,
) : GlobalClearanceMilestoneStore {
    override fun find(code: String): GlobalClearanceMilestone? {
        val hit = (cache.get(code) as GlobalClearanceMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalClearanceMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalClearanceMilestone> = inner.list()
}

fun newCachedGlobalClearanceMilestoneStore(inner: GlobalClearanceMilestoneStore, ttl: Int, clock: Clock): GlobalClearanceMilestoneStore {
    return CachedGlobalClearanceMilestoneStore(
        inner = inner,
        cache = newTtlCache<GlobalClearanceMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global clearance milestones. Empty fields match all. */
data class GlobalClearanceMilestoneQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalClearanceMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalClearanceMilestone>): MutableList<GlobalClearanceMilestone> {
        val matched = filter(items) { candidate: GlobalClearanceMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalClearanceMilestones(store: GlobalClearanceMilestoneStore, query: GlobalClearanceMilestoneQuery): MutableList<GlobalClearanceMilestone> {
    return query.apply(store.list())
}
