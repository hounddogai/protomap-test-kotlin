package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire milestones by code. */
interface TireMilestoneStore {
    fun find(code: String): TireMilestone?
    fun save(item: TireMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireMilestoneStore(
    var items: MutableMap<String, TireMilestone>,
    var order: MutableList<String>,
) : TireMilestoneStore {
    override fun find(code: String): TireMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireMilestone) {
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

    override fun list(): MutableList<TireMilestone> {
        val out = mutableListOf<TireMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireMilestoneStore(): TireMilestoneStore {
    return MemoryTireMilestoneStore(mutableMapOf<String, TireMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireMilestoneStore(
    var inner: TireMilestoneStore,
    var cache: TtlCache<TireMilestone>,
    var hits: Int,
    var misses: Int,
) : TireMilestoneStore {
    override fun find(code: String): TireMilestone? {
        val hit = (cache.get(code) as TireMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireMilestone> = inner.list()
}

fun newCachedTireMilestoneStore(inner: TireMilestoneStore, ttl: Int, clock: Clock): TireMilestoneStore {
    return CachedTireMilestoneStore(
        inner = inner,
        cache = newTtlCache<TireMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire milestones. Empty fields match all. */
data class TireMilestoneQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireMilestone): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireMilestone>): MutableList<TireMilestone> {
        val matched = filter(items) { candidate: TireMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireMilestones(store: TireMilestoneStore, query: TireMilestoneQuery): MutableList<TireMilestone> {
    return query.apply(store.list())
}
