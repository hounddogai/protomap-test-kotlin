package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual fuel surcharge milestones by code. */
interface ManualFuelSurchargeMilestoneStore {
    fun find(code: String): ManualFuelSurchargeMilestone?
    fun save(item: ManualFuelSurchargeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFuelSurchargeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFuelSurchargeMilestoneStore(
    var items: MutableMap<String, ManualFuelSurchargeMilestone>,
    var order: MutableList<String>,
) : ManualFuelSurchargeMilestoneStore {
    override fun find(code: String): ManualFuelSurchargeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFuelSurchargeMilestone) {
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

    override fun list(): MutableList<ManualFuelSurchargeMilestone> {
        val out = mutableListOf<ManualFuelSurchargeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFuelSurchargeMilestoneStore(): ManualFuelSurchargeMilestoneStore {
    return MemoryManualFuelSurchargeMilestoneStore(
        mutableMapOf<String, ManualFuelSurchargeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFuelSurchargeMilestoneStore(
    var inner: ManualFuelSurchargeMilestoneStore,
    var cache: TtlCache<ManualFuelSurchargeMilestone>,
    var hits: Int,
    var misses: Int,
) : ManualFuelSurchargeMilestoneStore {
    override fun find(code: String): ManualFuelSurchargeMilestone? {
        val hit = (cache.get(code) as ManualFuelSurchargeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFuelSurchargeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFuelSurchargeMilestone> = inner.list()
}

fun newCachedManualFuelSurchargeMilestoneStore(inner: ManualFuelSurchargeMilestoneStore, ttl: Int, clock: Clock): ManualFuelSurchargeMilestoneStore {
    return CachedManualFuelSurchargeMilestoneStore(
        inner = inner,
        cache = newTtlCache<ManualFuelSurchargeMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual fuel surcharge milestones. Empty fields match all. */
data class ManualFuelSurchargeMilestoneQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFuelSurchargeMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFuelSurchargeMilestone>): MutableList<ManualFuelSurchargeMilestone> {
        val matched = filter(items) { candidate: ManualFuelSurchargeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFuelSurchargeMilestones(store: ManualFuelSurchargeMilestoneStore, query: ManualFuelSurchargeMilestoneQuery): MutableList<ManualFuelSurchargeMilestone> {
    return query.apply(store.list())
}
