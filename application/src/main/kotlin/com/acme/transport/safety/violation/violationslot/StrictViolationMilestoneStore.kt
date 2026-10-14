package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict violation milestones by code. */
interface StrictViolationMilestoneStore {
    fun find(code: String): StrictViolationMilestone?
    fun save(item: StrictViolationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictViolationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictViolationMilestoneStore(
    var items: MutableMap<String, StrictViolationMilestone>,
    var order: MutableList<String>,
) : StrictViolationMilestoneStore {
    override fun find(code: String): StrictViolationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictViolationMilestone) {
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

    override fun list(): MutableList<StrictViolationMilestone> {
        val out = mutableListOf<StrictViolationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictViolationMilestoneStore(): StrictViolationMilestoneStore {
    return MemoryStrictViolationMilestoneStore(
        mutableMapOf<String, StrictViolationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictViolationMilestoneStore(
    var inner: StrictViolationMilestoneStore,
    var cache: TtlCache<StrictViolationMilestone>,
    var hits: Int,
    var misses: Int,
) : StrictViolationMilestoneStore {
    override fun find(code: String): StrictViolationMilestone? {
        val hit = (cache.get(code) as StrictViolationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictViolationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictViolationMilestone> = inner.list()
}

fun newCachedStrictViolationMilestoneStore(inner: StrictViolationMilestoneStore, ttl: Int, clock: Clock): StrictViolationMilestoneStore {
    return CachedStrictViolationMilestoneStore(
        inner = inner,
        cache = newTtlCache<StrictViolationMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict violation milestones. Empty fields match all. */
data class StrictViolationMilestoneQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictViolationMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<StrictViolationMilestone>): MutableList<StrictViolationMilestone> {
        val matched = filter(items) { candidate: StrictViolationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictViolationMilestones(store: StrictViolationMilestoneStore, query: StrictViolationMilestoneQuery): MutableList<StrictViolationMilestone> {
    return query.apply(store.list())
}
