package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched service plan groups by code. */
interface BatchedServicePlanGroupStore {
    fun find(code: String): BatchedServicePlanGroup?
    fun save(item: BatchedServicePlanGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServicePlanGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServicePlanGroupStore(
    var items: MutableMap<String, BatchedServicePlanGroup>,
    var order: MutableList<String>,
) : BatchedServicePlanGroupStore {
    override fun find(code: String): BatchedServicePlanGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServicePlanGroup) {
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

    override fun list(): MutableList<BatchedServicePlanGroup> {
        val out = mutableListOf<BatchedServicePlanGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServicePlanGroupStore(): BatchedServicePlanGroupStore {
    return MemoryBatchedServicePlanGroupStore(
        mutableMapOf<String, BatchedServicePlanGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServicePlanGroupStore(
    var inner: BatchedServicePlanGroupStore,
    var cache: TtlCache<BatchedServicePlanGroup>,
    var hits: Int,
    var misses: Int,
) : BatchedServicePlanGroupStore {
    override fun find(code: String): BatchedServicePlanGroup? {
        val hit = (cache.get(code) as BatchedServicePlanGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServicePlanGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServicePlanGroup> = inner.list()
}

fun newCachedBatchedServicePlanGroupStore(inner: BatchedServicePlanGroupStore, ttl: Int, clock: Clock): BatchedServicePlanGroupStore {
    return CachedBatchedServicePlanGroupStore(
        inner = inner,
        cache = newTtlCache<BatchedServicePlanGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service plan groups. Empty fields match all. */
data class BatchedServicePlanGroupQuery(
    var source: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServicePlanGroup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedServicePlanGroup>): MutableList<BatchedServicePlanGroup> {
        val matched = filter(items) { candidate: BatchedServicePlanGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServicePlanGroups(store: BatchedServicePlanGroupStore, query: BatchedServicePlanGroupQuery): MutableList<BatchedServicePlanGroup> {
    return query.apply(store.list())
}
