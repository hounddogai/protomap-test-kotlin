package com.acme.transport.safety.violation.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation milestones by code. */
interface ViolationMilestoneStore {
    fun find(code: String): ViolationMilestone?
    fun save(item: ViolationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationMilestoneStore(
    var items: MutableMap<String, ViolationMilestone>,
    var order: MutableList<String>,
) : ViolationMilestoneStore {
    override fun find(code: String): ViolationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationMilestone) {
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

    override fun list(): MutableList<ViolationMilestone> {
        val out = mutableListOf<ViolationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationMilestoneStore(): ViolationMilestoneStore {
    return MemoryViolationMilestoneStore(mutableMapOf<String, ViolationMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationMilestoneStore(
    var inner: ViolationMilestoneStore,
    var cache: TtlCache<ViolationMilestone>,
    var hits: Int,
    var misses: Int,
) : ViolationMilestoneStore {
    override fun find(code: String): ViolationMilestone? {
        val hit = (cache.get(code) as ViolationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationMilestone> = inner.list()
}

fun newCachedViolationMilestoneStore(inner: ViolationMilestoneStore, ttl: Int, clock: Clock): ViolationMilestoneStore {
    return CachedViolationMilestoneStore(
        inner = inner,
        cache = newTtlCache<ViolationMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation milestones. Empty fields match all. */
data class ViolationMilestoneQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ViolationMilestone>): MutableList<ViolationMilestone> {
        val matched = filter(items) { candidate: ViolationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationMilestones(store: ViolationMilestoneStore, query: ViolationMilestoneQuery): MutableList<ViolationMilestone> {
    return query.apply(store.list())
}
