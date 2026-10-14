package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered roster milestones by code. */
interface TieredRosterMilestoneStore {
    fun find(code: String): TieredRosterMilestone?
    fun save(item: TieredRosterMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRosterMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRosterMilestoneStore(
    var items: MutableMap<String, TieredRosterMilestone>,
    var order: MutableList<String>,
) : TieredRosterMilestoneStore {
    override fun find(code: String): TieredRosterMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRosterMilestone) {
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

    override fun list(): MutableList<TieredRosterMilestone> {
        val out = mutableListOf<TieredRosterMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRosterMilestoneStore(): TieredRosterMilestoneStore {
    return MemoryTieredRosterMilestoneStore(
        mutableMapOf<String, TieredRosterMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRosterMilestoneStore(
    var inner: TieredRosterMilestoneStore,
    var cache: TtlCache<TieredRosterMilestone>,
    var hits: Int,
    var misses: Int,
) : TieredRosterMilestoneStore {
    override fun find(code: String): TieredRosterMilestone? {
        val hit = (cache.get(code) as TieredRosterMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRosterMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRosterMilestone> = inner.list()
}

fun newCachedTieredRosterMilestoneStore(inner: TieredRosterMilestoneStore, ttl: Int, clock: Clock): TieredRosterMilestoneStore {
    return CachedTieredRosterMilestoneStore(
        inner = inner,
        cache = newTtlCache<TieredRosterMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered roster milestones. Empty fields match all. */
data class TieredRosterMilestoneQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRosterMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TieredRosterMilestone>): MutableList<TieredRosterMilestone> {
        val matched = filter(items) { candidate: TieredRosterMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRosterMilestones(store: TieredRosterMilestoneStore, query: TieredRosterMilestoneQuery): MutableList<TieredRosterMilestone> {
    return query.apply(store.list())
}
