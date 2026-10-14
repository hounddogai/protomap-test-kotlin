package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery milestones by code. */
interface RedeliveryMilestoneStore {
    fun find(code: String): RedeliveryMilestone?
    fun save(item: RedeliveryMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryMilestoneStore(
    var items: MutableMap<String, RedeliveryMilestone>,
    var order: MutableList<String>,
) : RedeliveryMilestoneStore {
    override fun find(code: String): RedeliveryMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryMilestone) {
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

    override fun list(): MutableList<RedeliveryMilestone> {
        val out = mutableListOf<RedeliveryMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryMilestoneStore(): RedeliveryMilestoneStore {
    return MemoryRedeliveryMilestoneStore(mutableMapOf<String, RedeliveryMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryMilestoneStore(
    var inner: RedeliveryMilestoneStore,
    var cache: TtlCache<RedeliveryMilestone>,
    var hits: Int,
    var misses: Int,
) : RedeliveryMilestoneStore {
    override fun find(code: String): RedeliveryMilestone? {
        val hit = (cache.get(code) as RedeliveryMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryMilestone> = inner.list()
}

fun newCachedRedeliveryMilestoneStore(inner: RedeliveryMilestoneStore, ttl: Int, clock: Clock): RedeliveryMilestoneStore {
    return CachedRedeliveryMilestoneStore(
        inner = inner,
        cache = newTtlCache<RedeliveryMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery milestones. Empty fields match all. */
data class RedeliveryMilestoneQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryMilestone>): MutableList<RedeliveryMilestone> {
        val matched = filter(items) { candidate: RedeliveryMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryMilestones(store: RedeliveryMilestoneStore, query: RedeliveryMilestoneQuery): MutableList<RedeliveryMilestone> {
    return query.apply(store.list())
}
