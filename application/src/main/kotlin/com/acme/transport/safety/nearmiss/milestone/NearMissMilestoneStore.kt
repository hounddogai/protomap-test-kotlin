package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss milestones by code. */
interface NearMissMilestoneStore {
    fun find(code: String): NearMissMilestone?
    fun save(item: NearMissMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissMilestoneStore(
    var items: MutableMap<String, NearMissMilestone>,
    var order: MutableList<String>,
) : NearMissMilestoneStore {
    override fun find(code: String): NearMissMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissMilestone) {
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

    override fun list(): MutableList<NearMissMilestone> {
        val out = mutableListOf<NearMissMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissMilestoneStore(): NearMissMilestoneStore {
    return MemoryNearMissMilestoneStore(mutableMapOf<String, NearMissMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissMilestoneStore(
    var inner: NearMissMilestoneStore,
    var cache: TtlCache<NearMissMilestone>,
    var hits: Int,
    var misses: Int,
) : NearMissMilestoneStore {
    override fun find(code: String): NearMissMilestone? {
        val hit = (cache.get(code) as NearMissMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissMilestone> = inner.list()
}

fun newCachedNearMissMilestoneStore(inner: NearMissMilestoneStore, ttl: Int, clock: Clock): NearMissMilestoneStore {
    return CachedNearMissMilestoneStore(
        inner = inner,
        cache = newTtlCache<NearMissMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss milestones. Empty fields match all. */
data class NearMissMilestoneQuery(
    var depot: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissMilestone>): MutableList<NearMissMilestone> {
        val matched = filter(items) { candidate: NearMissMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissMilestones(store: NearMissMilestoneStore, query: NearMissMilestoneQuery): MutableList<NearMissMilestone> {
    return query.apply(store.list())
}
