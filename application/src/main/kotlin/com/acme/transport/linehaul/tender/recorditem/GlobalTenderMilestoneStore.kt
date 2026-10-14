package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global tender milestones by code. */
interface GlobalTenderMilestoneStore {
    fun find(code: String): GlobalTenderMilestone?
    fun save(item: GlobalTenderMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTenderMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTenderMilestoneStore(
    var items: MutableMap<String, GlobalTenderMilestone>,
    var order: MutableList<String>,
) : GlobalTenderMilestoneStore {
    override fun find(code: String): GlobalTenderMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTenderMilestone) {
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

    override fun list(): MutableList<GlobalTenderMilestone> {
        val out = mutableListOf<GlobalTenderMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTenderMilestoneStore(): GlobalTenderMilestoneStore {
    return MemoryGlobalTenderMilestoneStore(
        mutableMapOf<String, GlobalTenderMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTenderMilestoneStore(
    var inner: GlobalTenderMilestoneStore,
    var cache: TtlCache<GlobalTenderMilestone>,
    var hits: Int,
    var misses: Int,
) : GlobalTenderMilestoneStore {
    override fun find(code: String): GlobalTenderMilestone? {
        val hit = (cache.get(code) as GlobalTenderMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTenderMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTenderMilestone> = inner.list()
}

fun newCachedGlobalTenderMilestoneStore(inner: GlobalTenderMilestoneStore, ttl: Int, clock: Clock): GlobalTenderMilestoneStore {
    return CachedGlobalTenderMilestoneStore(
        inner = inner,
        cache = newTtlCache<GlobalTenderMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tender milestones. Empty fields match all. */
data class GlobalTenderMilestoneQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTenderMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTenderMilestone>): MutableList<GlobalTenderMilestone> {
        val matched = filter(items) { candidate: GlobalTenderMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTenderMilestones(store: GlobalTenderMilestoneStore, query: GlobalTenderMilestoneQuery): MutableList<GlobalTenderMilestone> {
    return query.apply(store.list())
}
