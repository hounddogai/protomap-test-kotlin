package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary node milestones by code. */
interface PrimaryNodeMilestoneStore {
    fun find(code: String): PrimaryNodeMilestone?
    fun save(item: PrimaryNodeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNodeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNodeMilestoneStore(
    var items: MutableMap<String, PrimaryNodeMilestone>,
    var order: MutableList<String>,
) : PrimaryNodeMilestoneStore {
    override fun find(code: String): PrimaryNodeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNodeMilestone) {
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

    override fun list(): MutableList<PrimaryNodeMilestone> {
        val out = mutableListOf<PrimaryNodeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNodeMilestoneStore(): PrimaryNodeMilestoneStore {
    return MemoryPrimaryNodeMilestoneStore(
        mutableMapOf<String, PrimaryNodeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNodeMilestoneStore(
    var inner: PrimaryNodeMilestoneStore,
    var cache: TtlCache<PrimaryNodeMilestone>,
    var hits: Int,
    var misses: Int,
) : PrimaryNodeMilestoneStore {
    override fun find(code: String): PrimaryNodeMilestone? {
        val hit = (cache.get(code) as PrimaryNodeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNodeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNodeMilestone> = inner.list()
}

fun newCachedPrimaryNodeMilestoneStore(inner: PrimaryNodeMilestoneStore, ttl: Int, clock: Clock): PrimaryNodeMilestoneStore {
    return CachedPrimaryNodeMilestoneStore(
        inner = inner,
        cache = newTtlCache<PrimaryNodeMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary node milestones. Empty fields match all. */
data class PrimaryNodeMilestoneQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNodeMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryNodeMilestone>): MutableList<PrimaryNodeMilestone> {
        val matched = filter(items) { candidate: PrimaryNodeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNodeMilestones(store: PrimaryNodeMilestoneStore, query: PrimaryNodeMilestoneQuery): MutableList<PrimaryNodeMilestone> {
    return query.apply(store.list())
}
