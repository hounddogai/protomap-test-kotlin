package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge milestones by code. */
interface FuelSurchargeMilestoneStore {
    fun find(code: String): FuelSurchargeMilestone?
    fun save(item: FuelSurchargeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeMilestoneStore(
    var items: MutableMap<String, FuelSurchargeMilestone>,
    var order: MutableList<String>,
) : FuelSurchargeMilestoneStore {
    override fun find(code: String): FuelSurchargeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeMilestone) {
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

    override fun list(): MutableList<FuelSurchargeMilestone> {
        val out = mutableListOf<FuelSurchargeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeMilestoneStore(): FuelSurchargeMilestoneStore {
    return MemoryFuelSurchargeMilestoneStore(
        mutableMapOf<String, FuelSurchargeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeMilestoneStore(
    var inner: FuelSurchargeMilestoneStore,
    var cache: TtlCache<FuelSurchargeMilestone>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeMilestoneStore {
    override fun find(code: String): FuelSurchargeMilestone? {
        val hit = (cache.get(code) as FuelSurchargeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeMilestone> = inner.list()
}

fun newCachedFuelSurchargeMilestoneStore(inner: FuelSurchargeMilestoneStore, ttl: Int, clock: Clock): FuelSurchargeMilestoneStore {
    return CachedFuelSurchargeMilestoneStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge milestones. Empty fields match all. */
data class FuelSurchargeMilestoneQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FuelSurchargeMilestone>): MutableList<FuelSurchargeMilestone> {
        val matched = filter(items) { candidate: FuelSurchargeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeMilestones(store: FuelSurchargeMilestoneStore, query: FuelSurchargeMilestoneQuery): MutableList<FuelSurchargeMilestone> {
    return query.apply(store.list())
}
