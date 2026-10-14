package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock milestones by code. */
interface CrossDockMilestoneStore {
    fun find(code: String): CrossDockMilestone?
    fun save(item: CrossDockMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockMilestoneStore(
    var items: MutableMap<String, CrossDockMilestone>,
    var order: MutableList<String>,
) : CrossDockMilestoneStore {
    override fun find(code: String): CrossDockMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockMilestone) {
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

    override fun list(): MutableList<CrossDockMilestone> {
        val out = mutableListOf<CrossDockMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockMilestoneStore(): CrossDockMilestoneStore {
    return MemoryCrossDockMilestoneStore(mutableMapOf<String, CrossDockMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockMilestoneStore(
    var inner: CrossDockMilestoneStore,
    var cache: TtlCache<CrossDockMilestone>,
    var hits: Int,
    var misses: Int,
) : CrossDockMilestoneStore {
    override fun find(code: String): CrossDockMilestone? {
        val hit = (cache.get(code) as CrossDockMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockMilestone> = inner.list()
}

fun newCachedCrossDockMilestoneStore(inner: CrossDockMilestoneStore, ttl: Int, clock: Clock): CrossDockMilestoneStore {
    return CachedCrossDockMilestoneStore(
        inner = inner,
        cache = newTtlCache<CrossDockMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock milestones. Empty fields match all. */
data class CrossDockMilestoneQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockMilestone>): MutableList<CrossDockMilestone> {
        val matched = filter(items) { candidate: CrossDockMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockMilestones(store: CrossDockMilestoneStore, query: CrossDockMilestoneQuery): MutableList<CrossDockMilestone> {
    return query.apply(store.list())
}
