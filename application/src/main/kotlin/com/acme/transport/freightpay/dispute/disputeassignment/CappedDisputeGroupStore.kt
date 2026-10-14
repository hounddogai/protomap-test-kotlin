package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped dispute groups by code. */
interface CappedDisputeGroupStore {
    fun find(code: String): CappedDisputeGroup?
    fun save(item: CappedDisputeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDisputeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDisputeGroupStore(
    var items: MutableMap<String, CappedDisputeGroup>,
    var order: MutableList<String>,
) : CappedDisputeGroupStore {
    override fun find(code: String): CappedDisputeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDisputeGroup) {
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

    override fun list(): MutableList<CappedDisputeGroup> {
        val out = mutableListOf<CappedDisputeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDisputeGroupStore(): CappedDisputeGroupStore {
    return MemoryCappedDisputeGroupStore(mutableMapOf<String, CappedDisputeGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDisputeGroupStore(
    var inner: CappedDisputeGroupStore,
    var cache: TtlCache<CappedDisputeGroup>,
    var hits: Int,
    var misses: Int,
) : CappedDisputeGroupStore {
    override fun find(code: String): CappedDisputeGroup? {
        val hit = (cache.get(code) as CappedDisputeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDisputeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDisputeGroup> = inner.list()
}

fun newCachedCappedDisputeGroupStore(inner: CappedDisputeGroupStore, ttl: Int, clock: Clock): CappedDisputeGroupStore {
    return CachedCappedDisputeGroupStore(
        inner = inner,
        cache = newTtlCache<CappedDisputeGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dispute groups. Empty fields match all. */
data class CappedDisputeGroupQuery(
    var category: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDisputeGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDisputeGroup>): MutableList<CappedDisputeGroup> {
        val matched = filter(items) { candidate: CappedDisputeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDisputeGroups(store: CappedDisputeGroupStore, query: CappedDisputeGroupQuery): MutableList<CappedDisputeGroup> {
    return query.apply(store.list())
}
