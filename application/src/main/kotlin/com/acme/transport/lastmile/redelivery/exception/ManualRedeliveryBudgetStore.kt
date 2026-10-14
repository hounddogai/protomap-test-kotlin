package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual redelivery budgets by code. */
interface ManualRedeliveryBudgetStore {
    fun find(code: String): ManualRedeliveryBudget?
    fun save(item: ManualRedeliveryBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRedeliveryBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRedeliveryBudgetStore(
    var items: MutableMap<String, ManualRedeliveryBudget>,
    var order: MutableList<String>,
) : ManualRedeliveryBudgetStore {
    override fun find(code: String): ManualRedeliveryBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRedeliveryBudget) {
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

    override fun list(): MutableList<ManualRedeliveryBudget> {
        val out = mutableListOf<ManualRedeliveryBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRedeliveryBudgetStore(): ManualRedeliveryBudgetStore {
    return MemoryManualRedeliveryBudgetStore(
        mutableMapOf<String, ManualRedeliveryBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRedeliveryBudgetStore(
    var inner: ManualRedeliveryBudgetStore,
    var cache: TtlCache<ManualRedeliveryBudget>,
    var hits: Int,
    var misses: Int,
) : ManualRedeliveryBudgetStore {
    override fun find(code: String): ManualRedeliveryBudget? {
        val hit = (cache.get(code) as ManualRedeliveryBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRedeliveryBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRedeliveryBudget> = inner.list()
}

fun newCachedManualRedeliveryBudgetStore(inner: ManualRedeliveryBudgetStore, ttl: Int, clock: Clock): ManualRedeliveryBudgetStore {
    return CachedManualRedeliveryBudgetStore(
        inner = inner,
        cache = newTtlCache<ManualRedeliveryBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual redelivery budgets. Empty fields match all. */
data class ManualRedeliveryBudgetQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRedeliveryBudget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRedeliveryBudget>): MutableList<ManualRedeliveryBudget> {
        val matched = filter(items) { candidate: ManualRedeliveryBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRedeliveryBudgets(store: ManualRedeliveryBudgetStore, query: ManualRedeliveryBudgetQuery): MutableList<ManualRedeliveryBudget> {
    return query.apply(store.list())
}
