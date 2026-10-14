package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional near miss milestones by code. */
interface RegionalNearMissMilestoneStore {
    fun find(code: String): RegionalNearMissMilestone?
    fun save(item: RegionalNearMissMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalNearMissMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalNearMissMilestoneStore(
    var items: MutableMap<String, RegionalNearMissMilestone>,
    var order: MutableList<String>,
) : RegionalNearMissMilestoneStore {
    override fun find(code: String): RegionalNearMissMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalNearMissMilestone) {
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

    override fun list(): MutableList<RegionalNearMissMilestone> {
        val out = mutableListOf<RegionalNearMissMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalNearMissMilestoneStore(): RegionalNearMissMilestoneStore {
    return MemoryRegionalNearMissMilestoneStore(
        mutableMapOf<String, RegionalNearMissMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalNearMissMilestoneStore(
    var inner: RegionalNearMissMilestoneStore,
    var cache: TtlCache<RegionalNearMissMilestone>,
    var hits: Int,
    var misses: Int,
) : RegionalNearMissMilestoneStore {
    override fun find(code: String): RegionalNearMissMilestone? {
        val hit = (cache.get(code) as RegionalNearMissMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalNearMissMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalNearMissMilestone> = inner.list()
}

fun newCachedRegionalNearMissMilestoneStore(inner: RegionalNearMissMilestoneStore, ttl: Int, clock: Clock): RegionalNearMissMilestoneStore {
    return CachedRegionalNearMissMilestoneStore(
        inner = inner,
        cache = newTtlCache<RegionalNearMissMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional near miss milestones. Empty fields match all. */
data class RegionalNearMissMilestoneQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalNearMissMilestone): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalNearMissMilestone>): MutableList<RegionalNearMissMilestone> {
        val matched = filter(items) { candidate: RegionalNearMissMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalNearMissMilestones(store: RegionalNearMissMilestoneStore, query: RegionalNearMissMilestoneQuery): MutableList<RegionalNearMissMilestone> {
    return query.apply(store.list())
}
