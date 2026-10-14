package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered dwell milestones by code. */
interface TieredDwellMilestoneStore {
    fun find(code: String): TieredDwellMilestone?
    fun save(item: TieredDwellMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDwellMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDwellMilestoneStore(
    var items: MutableMap<String, TieredDwellMilestone>,
    var order: MutableList<String>,
) : TieredDwellMilestoneStore {
    override fun find(code: String): TieredDwellMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDwellMilestone) {
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

    override fun list(): MutableList<TieredDwellMilestone> {
        val out = mutableListOf<TieredDwellMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDwellMilestoneStore(): TieredDwellMilestoneStore {
    return MemoryTieredDwellMilestoneStore(
        mutableMapOf<String, TieredDwellMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDwellMilestoneStore(
    var inner: TieredDwellMilestoneStore,
    var cache: TtlCache<TieredDwellMilestone>,
    var hits: Int,
    var misses: Int,
) : TieredDwellMilestoneStore {
    override fun find(code: String): TieredDwellMilestone? {
        val hit = (cache.get(code) as TieredDwellMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDwellMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDwellMilestone> = inner.list()
}

fun newCachedTieredDwellMilestoneStore(inner: TieredDwellMilestoneStore, ttl: Int, clock: Clock): TieredDwellMilestoneStore {
    return CachedTieredDwellMilestoneStore(
        inner = inner,
        cache = newTtlCache<TieredDwellMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dwell milestones. Empty fields match all. */
data class TieredDwellMilestoneQuery(
    var country: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDwellMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDwellMilestone>): MutableList<TieredDwellMilestone> {
        val matched = filter(items) { candidate: TieredDwellMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDwellMilestones(store: TieredDwellMilestoneStore, query: TieredDwellMilestoneQuery): MutableList<TieredDwellMilestone> {
    return query.apply(store.list())
}
