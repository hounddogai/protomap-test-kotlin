package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback fuel surcharge milestones by code. */
interface FallbackFuelSurchargeMilestoneStore {
    fun find(code: String): FallbackFuelSurchargeMilestone?
    fun save(item: FallbackFuelSurchargeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFuelSurchargeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFuelSurchargeMilestoneStore(
    var items: MutableMap<String, FallbackFuelSurchargeMilestone>,
    var order: MutableList<String>,
) : FallbackFuelSurchargeMilestoneStore {
    override fun find(code: String): FallbackFuelSurchargeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFuelSurchargeMilestone) {
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

    override fun list(): MutableList<FallbackFuelSurchargeMilestone> {
        val out = mutableListOf<FallbackFuelSurchargeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFuelSurchargeMilestoneStore(): FallbackFuelSurchargeMilestoneStore {
    return MemoryFallbackFuelSurchargeMilestoneStore(
        mutableMapOf<String, FallbackFuelSurchargeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFuelSurchargeMilestoneStore(
    var inner: FallbackFuelSurchargeMilestoneStore,
    var cache: TtlCache<FallbackFuelSurchargeMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackFuelSurchargeMilestoneStore {
    override fun find(code: String): FallbackFuelSurchargeMilestone? {
        val hit = (cache.get(code) as FallbackFuelSurchargeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFuelSurchargeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFuelSurchargeMilestone> = inner.list()
}

fun newCachedFallbackFuelSurchargeMilestoneStore(inner: FallbackFuelSurchargeMilestoneStore, ttl: Int, clock: Clock): FallbackFuelSurchargeMilestoneStore {
    return CachedFallbackFuelSurchargeMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackFuelSurchargeMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback fuel surcharge milestones. Empty fields match all. */
data class FallbackFuelSurchargeMilestoneQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFuelSurchargeMilestone): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackFuelSurchargeMilestone>): MutableList<FallbackFuelSurchargeMilestone> {
        val matched = filter(items) { candidate: FallbackFuelSurchargeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFuelSurchargeMilestones(store: FallbackFuelSurchargeMilestoneStore, query: FallbackFuelSurchargeMilestoneQuery): MutableList<FallbackFuelSurchargeMilestone> {
    return query.apply(store.list())
}
