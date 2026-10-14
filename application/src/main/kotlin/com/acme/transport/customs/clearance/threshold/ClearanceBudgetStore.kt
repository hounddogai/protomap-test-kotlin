package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance budgets by code. */
interface ClearanceBudgetStore {
    fun find(code: String): ClearanceBudget?
    fun save(item: ClearanceBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceBudgetStore(
    var items: MutableMap<String, ClearanceBudget>,
    var order: MutableList<String>,
) : ClearanceBudgetStore {
    override fun find(code: String): ClearanceBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceBudget) {
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

    override fun list(): MutableList<ClearanceBudget> {
        val out = mutableListOf<ClearanceBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceBudgetStore(): ClearanceBudgetStore {
    return MemoryClearanceBudgetStore(mutableMapOf<String, ClearanceBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceBudgetStore(
    var inner: ClearanceBudgetStore,
    var cache: TtlCache<ClearanceBudget>,
    var hits: Int,
    var misses: Int,
) : ClearanceBudgetStore {
    override fun find(code: String): ClearanceBudget? {
        val hit = (cache.get(code) as ClearanceBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceBudget> = inner.list()
}

fun newCachedClearanceBudgetStore(inner: ClearanceBudgetStore, ttl: Int, clock: Clock): ClearanceBudgetStore {
    return CachedClearanceBudgetStore(
        inner = inner,
        cache = newTtlCache<ClearanceBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance budgets. Empty fields match all. */
data class ClearanceBudgetQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceBudget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ClearanceBudget>): MutableList<ClearanceBudget> {
        val matched = filter(items) { candidate: ClearanceBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceBudgets(store: ClearanceBudgetStore, query: ClearanceBudgetQuery): MutableList<ClearanceBudget> {
    return query.apply(store.list())
}
