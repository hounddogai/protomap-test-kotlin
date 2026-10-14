package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute milestones by code. */
interface DisputeMilestoneStore {
    fun find(code: String): DisputeMilestone?
    fun save(item: DisputeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeMilestoneStore(
    var items: MutableMap<String, DisputeMilestone>,
    var order: MutableList<String>,
) : DisputeMilestoneStore {
    override fun find(code: String): DisputeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeMilestone) {
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

    override fun list(): MutableList<DisputeMilestone> {
        val out = mutableListOf<DisputeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeMilestoneStore(): DisputeMilestoneStore {
    return MemoryDisputeMilestoneStore(mutableMapOf<String, DisputeMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeMilestoneStore(
    var inner: DisputeMilestoneStore,
    var cache: TtlCache<DisputeMilestone>,
    var hits: Int,
    var misses: Int,
) : DisputeMilestoneStore {
    override fun find(code: String): DisputeMilestone? {
        val hit = (cache.get(code) as DisputeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeMilestone> = inner.list()
}

fun newCachedDisputeMilestoneStore(inner: DisputeMilestoneStore, ttl: Int, clock: Clock): DisputeMilestoneStore {
    return CachedDisputeMilestoneStore(
        inner = inner,
        cache = newTtlCache<DisputeMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute milestones. Empty fields match all. */
data class DisputeMilestoneQuery(
    var lane: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeMilestone): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DisputeMilestone>): MutableList<DisputeMilestone> {
        val matched = filter(items) { candidate: DisputeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeMilestones(store: DisputeMilestoneStore, query: DisputeMilestoneQuery): MutableList<DisputeMilestone> {
    return query.apply(store.list())
}
