package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending near miss milestones by code. */
interface PendingNearMissMilestoneStore {
    fun find(code: String): PendingNearMissMilestone?
    fun save(item: PendingNearMissMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNearMissMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNearMissMilestoneStore(
    var items: MutableMap<String, PendingNearMissMilestone>,
    var order: MutableList<String>,
) : PendingNearMissMilestoneStore {
    override fun find(code: String): PendingNearMissMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNearMissMilestone) {
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

    override fun list(): MutableList<PendingNearMissMilestone> {
        val out = mutableListOf<PendingNearMissMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNearMissMilestoneStore(): PendingNearMissMilestoneStore {
    return MemoryPendingNearMissMilestoneStore(
        mutableMapOf<String, PendingNearMissMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNearMissMilestoneStore(
    var inner: PendingNearMissMilestoneStore,
    var cache: TtlCache<PendingNearMissMilestone>,
    var hits: Int,
    var misses: Int,
) : PendingNearMissMilestoneStore {
    override fun find(code: String): PendingNearMissMilestone? {
        val hit = (cache.get(code) as PendingNearMissMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNearMissMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNearMissMilestone> = inner.list()
}

fun newCachedPendingNearMissMilestoneStore(inner: PendingNearMissMilestoneStore, ttl: Int, clock: Clock): PendingNearMissMilestoneStore {
    return CachedPendingNearMissMilestoneStore(
        inner = inner,
        cache = newTtlCache<PendingNearMissMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending near miss milestones. Empty fields match all. */
data class PendingNearMissMilestoneQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNearMissMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNearMissMilestone>): MutableList<PendingNearMissMilestone> {
        val matched = filter(items) { candidate: PendingNearMissMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNearMissMilestones(store: PendingNearMissMilestoneStore, query: PendingNearMissMilestoneQuery): MutableList<PendingNearMissMilestone> {
    return query.apply(store.list())
}
