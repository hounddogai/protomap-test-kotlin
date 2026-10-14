package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel surcharge milestones by code. */
interface RollingFuelSurchargeMilestoneStore {
    fun find(code: String): RollingFuelSurchargeMilestone?
    fun save(item: RollingFuelSurchargeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelSurchargeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelSurchargeMilestoneStore(
    var items: MutableMap<String, RollingFuelSurchargeMilestone>,
    var order: MutableList<String>,
) : RollingFuelSurchargeMilestoneStore {
    override fun find(code: String): RollingFuelSurchargeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelSurchargeMilestone) {
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

    override fun list(): MutableList<RollingFuelSurchargeMilestone> {
        val out = mutableListOf<RollingFuelSurchargeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelSurchargeMilestoneStore(): RollingFuelSurchargeMilestoneStore {
    return MemoryRollingFuelSurchargeMilestoneStore(
        mutableMapOf<String, RollingFuelSurchargeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelSurchargeMilestoneStore(
    var inner: RollingFuelSurchargeMilestoneStore,
    var cache: TtlCache<RollingFuelSurchargeMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingFuelSurchargeMilestoneStore {
    override fun find(code: String): RollingFuelSurchargeMilestone? {
        val hit = (cache.get(code) as RollingFuelSurchargeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelSurchargeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelSurchargeMilestone> = inner.list()
}

fun newCachedRollingFuelSurchargeMilestoneStore(inner: RollingFuelSurchargeMilestoneStore, ttl: Int, clock: Clock): RollingFuelSurchargeMilestoneStore {
    return CachedRollingFuelSurchargeMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingFuelSurchargeMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel surcharge milestones. Empty fields match all. */
data class RollingFuelSurchargeMilestoneQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelSurchargeMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFuelSurchargeMilestone>): MutableList<RollingFuelSurchargeMilestone> {
        val matched = filter(items) { candidate: RollingFuelSurchargeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelSurchargeMilestones(store: RollingFuelSurchargeMilestoneStore, query: RollingFuelSurchargeMilestoneQuery): MutableList<RollingFuelSurchargeMilestone> {
    return query.apply(store.list())
}
