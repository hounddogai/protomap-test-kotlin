package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity budgets by code. */
interface CapacityBudgetStore {
    fun find(code: String): CapacityBudget?
    fun save(item: CapacityBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityBudgetStore(
    var items: MutableMap<String, CapacityBudget>,
    var order: MutableList<String>,
) : CapacityBudgetStore {
    override fun find(code: String): CapacityBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityBudget) {
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

    override fun list(): MutableList<CapacityBudget> {
        val out = mutableListOf<CapacityBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityBudgetStore(): CapacityBudgetStore {
    return MemoryCapacityBudgetStore(mutableMapOf<String, CapacityBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityBudgetStore(
    var inner: CapacityBudgetStore,
    var cache: TtlCache<CapacityBudget>,
    var hits: Int,
    var misses: Int,
) : CapacityBudgetStore {
    override fun find(code: String): CapacityBudget? {
        val hit = (cache.get(code) as CapacityBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityBudget> = inner.list()
}

fun newCachedCapacityBudgetStore(inner: CapacityBudgetStore, ttl: Int, clock: Clock): CapacityBudgetStore {
    return CachedCapacityBudgetStore(
        inner = inner,
        cache = newTtlCache<CapacityBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity budgets. Empty fields match all. */
data class CapacityBudgetQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityBudget>): MutableList<CapacityBudget> {
        val matched = filter(items) { candidate: CapacityBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityBudgets(store: CapacityBudgetStore, query: CapacityBudgetQuery): MutableList<CapacityBudget> {
    return query.apply(store.list())
}
