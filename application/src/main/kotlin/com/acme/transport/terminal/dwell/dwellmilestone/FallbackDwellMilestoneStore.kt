package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell milestones by code. */
interface FallbackDwellMilestoneStore {
    fun find(code: String): FallbackDwellMilestone?
    fun save(item: FallbackDwellMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellMilestoneStore(
    var items: MutableMap<String, FallbackDwellMilestone>,
    var order: MutableList<String>,
) : FallbackDwellMilestoneStore {
    override fun find(code: String): FallbackDwellMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellMilestone) {
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

    override fun list(): MutableList<FallbackDwellMilestone> {
        val out = mutableListOf<FallbackDwellMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellMilestoneStore(): FallbackDwellMilestoneStore {
    return MemoryFallbackDwellMilestoneStore(
        mutableMapOf<String, FallbackDwellMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellMilestoneStore(
    var inner: FallbackDwellMilestoneStore,
    var cache: TtlCache<FallbackDwellMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellMilestoneStore {
    override fun find(code: String): FallbackDwellMilestone? {
        val hit = (cache.get(code) as FallbackDwellMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellMilestone> = inner.list()
}

fun newCachedFallbackDwellMilestoneStore(inner: FallbackDwellMilestoneStore, ttl: Int, clock: Clock): FallbackDwellMilestoneStore {
    return CachedFallbackDwellMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell milestones. Empty fields match all. */
data class FallbackDwellMilestoneQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDwellMilestone>): MutableList<FallbackDwellMilestone> {
        val matched = filter(items) { candidate: FallbackDwellMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellMilestones(store: FallbackDwellMilestoneStore, query: FallbackDwellMilestoneQuery): MutableList<FallbackDwellMilestone> {
    return query.apply(store.list())
}
