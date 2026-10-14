package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped sortation milestones by code. */
interface CappedSortationMilestoneStore {
    fun find(code: String): CappedSortationMilestone?
    fun save(item: CappedSortationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedSortationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedSortationMilestoneStore(
    var items: MutableMap<String, CappedSortationMilestone>,
    var order: MutableList<String>,
) : CappedSortationMilestoneStore {
    override fun find(code: String): CappedSortationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedSortationMilestone) {
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

    override fun list(): MutableList<CappedSortationMilestone> {
        val out = mutableListOf<CappedSortationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedSortationMilestoneStore(): CappedSortationMilestoneStore {
    return MemoryCappedSortationMilestoneStore(
        mutableMapOf<String, CappedSortationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedSortationMilestoneStore(
    var inner: CappedSortationMilestoneStore,
    var cache: TtlCache<CappedSortationMilestone>,
    var hits: Int,
    var misses: Int,
) : CappedSortationMilestoneStore {
    override fun find(code: String): CappedSortationMilestone? {
        val hit = (cache.get(code) as CappedSortationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedSortationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedSortationMilestone> = inner.list()
}

fun newCachedCappedSortationMilestoneStore(inner: CappedSortationMilestoneStore, ttl: Int, clock: Clock): CappedSortationMilestoneStore {
    return CachedCappedSortationMilestoneStore(
        inner = inner,
        cache = newTtlCache<CappedSortationMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped sortation milestones. Empty fields match all. */
data class CappedSortationMilestoneQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedSortationMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedSortationMilestone>): MutableList<CappedSortationMilestone> {
        val matched = filter(items) { candidate: CappedSortationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedSortationMilestones(store: CappedSortationMilestoneStore, query: CappedSortationMilestoneQuery): MutableList<CappedSortationMilestone> {
    return query.apply(store.list())
}
