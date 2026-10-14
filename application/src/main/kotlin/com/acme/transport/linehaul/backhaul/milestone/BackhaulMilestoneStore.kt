package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul milestones by code. */
interface BackhaulMilestoneStore {
    fun find(code: String): BackhaulMilestone?
    fun save(item: BackhaulMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulMilestoneStore(
    var items: MutableMap<String, BackhaulMilestone>,
    var order: MutableList<String>,
) : BackhaulMilestoneStore {
    override fun find(code: String): BackhaulMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulMilestone) {
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

    override fun list(): MutableList<BackhaulMilestone> {
        val out = mutableListOf<BackhaulMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulMilestoneStore(): BackhaulMilestoneStore {
    return MemoryBackhaulMilestoneStore(mutableMapOf<String, BackhaulMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulMilestoneStore(
    var inner: BackhaulMilestoneStore,
    var cache: TtlCache<BackhaulMilestone>,
    var hits: Int,
    var misses: Int,
) : BackhaulMilestoneStore {
    override fun find(code: String): BackhaulMilestone? {
        val hit = (cache.get(code) as BackhaulMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulMilestone> = inner.list()
}

fun newCachedBackhaulMilestoneStore(inner: BackhaulMilestoneStore, ttl: Int, clock: Clock): BackhaulMilestoneStore {
    return CachedBackhaulMilestoneStore(
        inner = inner,
        cache = newTtlCache<BackhaulMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul milestones. Empty fields match all. */
data class BackhaulMilestoneQuery(
    var depot: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulMilestone>): MutableList<BackhaulMilestone> {
        val matched = filter(items) { candidate: BackhaulMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulMilestones(store: BackhaulMilestoneStore, query: BackhaulMilestoneQuery): MutableList<BackhaulMilestone> {
    return query.apply(store.list())
}
