package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard milestones by code. */
interface TrailerYardMilestoneStore {
    fun find(code: String): TrailerYardMilestone?
    fun save(item: TrailerYardMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardMilestoneStore(
    var items: MutableMap<String, TrailerYardMilestone>,
    var order: MutableList<String>,
) : TrailerYardMilestoneStore {
    override fun find(code: String): TrailerYardMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardMilestone) {
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

    override fun list(): MutableList<TrailerYardMilestone> {
        val out = mutableListOf<TrailerYardMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardMilestoneStore(): TrailerYardMilestoneStore {
    return MemoryTrailerYardMilestoneStore(
        mutableMapOf<String, TrailerYardMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardMilestoneStore(
    var inner: TrailerYardMilestoneStore,
    var cache: TtlCache<TrailerYardMilestone>,
    var hits: Int,
    var misses: Int,
) : TrailerYardMilestoneStore {
    override fun find(code: String): TrailerYardMilestone? {
        val hit = (cache.get(code) as TrailerYardMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardMilestone> = inner.list()
}

fun newCachedTrailerYardMilestoneStore(inner: TrailerYardMilestoneStore, ttl: Int, clock: Clock): TrailerYardMilestoneStore {
    return CachedTrailerYardMilestoneStore(
        inner = inner,
        cache = newTtlCache<TrailerYardMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard milestones. Empty fields match all. */
data class TrailerYardMilestoneQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardMilestone>): MutableList<TrailerYardMilestone> {
        val matched = filter(items) { candidate: TrailerYardMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardMilestones(store: TrailerYardMilestoneStore, query: TrailerYardMilestoneQuery): MutableList<TrailerYardMilestone> {
    return query.apply(store.list())
}
