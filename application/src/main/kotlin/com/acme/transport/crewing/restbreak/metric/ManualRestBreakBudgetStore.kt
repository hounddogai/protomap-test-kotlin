package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual rest break budgets by code. */
interface ManualRestBreakBudgetStore {
    fun find(code: String): ManualRestBreakBudget?
    fun save(item: ManualRestBreakBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRestBreakBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRestBreakBudgetStore(
    var items: MutableMap<String, ManualRestBreakBudget>,
    var order: MutableList<String>,
) : ManualRestBreakBudgetStore {
    override fun find(code: String): ManualRestBreakBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRestBreakBudget) {
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

    override fun list(): MutableList<ManualRestBreakBudget> {
        val out = mutableListOf<ManualRestBreakBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRestBreakBudgetStore(): ManualRestBreakBudgetStore {
    return MemoryManualRestBreakBudgetStore(
        mutableMapOf<String, ManualRestBreakBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRestBreakBudgetStore(
    var inner: ManualRestBreakBudgetStore,
    var cache: TtlCache<ManualRestBreakBudget>,
    var hits: Int,
    var misses: Int,
) : ManualRestBreakBudgetStore {
    override fun find(code: String): ManualRestBreakBudget? {
        val hit = (cache.get(code) as ManualRestBreakBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRestBreakBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRestBreakBudget> = inner.list()
}

fun newCachedManualRestBreakBudgetStore(inner: ManualRestBreakBudgetStore, ttl: Int, clock: Clock): ManualRestBreakBudgetStore {
    return CachedManualRestBreakBudgetStore(
        inner = inner,
        cache = newTtlCache<ManualRestBreakBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual rest break budgets. Empty fields match all. */
data class ManualRestBreakBudgetQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRestBreakBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRestBreakBudget>): MutableList<ManualRestBreakBudget> {
        val matched = filter(items) { candidate: ManualRestBreakBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRestBreakBudgets(store: ManualRestBreakBudgetStore, query: ManualRestBreakBudgetQuery): MutableList<ManualRestBreakBudget> {
    return query.apply(store.list())
}
