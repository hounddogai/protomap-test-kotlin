package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rest break budgets by code. */
interface GlobalRestBreakBudgetStore {
    fun find(code: String): GlobalRestBreakBudget?
    fun save(item: GlobalRestBreakBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRestBreakBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRestBreakBudgetStore(
    var items: MutableMap<String, GlobalRestBreakBudget>,
    var order: MutableList<String>,
) : GlobalRestBreakBudgetStore {
    override fun find(code: String): GlobalRestBreakBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRestBreakBudget) {
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

    override fun list(): MutableList<GlobalRestBreakBudget> {
        val out = mutableListOf<GlobalRestBreakBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRestBreakBudgetStore(): GlobalRestBreakBudgetStore {
    return MemoryGlobalRestBreakBudgetStore(
        mutableMapOf<String, GlobalRestBreakBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRestBreakBudgetStore(
    var inner: GlobalRestBreakBudgetStore,
    var cache: TtlCache<GlobalRestBreakBudget>,
    var hits: Int,
    var misses: Int,
) : GlobalRestBreakBudgetStore {
    override fun find(code: String): GlobalRestBreakBudget? {
        val hit = (cache.get(code) as GlobalRestBreakBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRestBreakBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRestBreakBudget> = inner.list()
}

fun newCachedGlobalRestBreakBudgetStore(inner: GlobalRestBreakBudgetStore, ttl: Int, clock: Clock): GlobalRestBreakBudgetStore {
    return CachedGlobalRestBreakBudgetStore(
        inner = inner,
        cache = newTtlCache<GlobalRestBreakBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rest break budgets. Empty fields match all. */
data class GlobalRestBreakBudgetQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRestBreakBudget): Boolean {
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

    fun apply(items: MutableList<GlobalRestBreakBudget>): MutableList<GlobalRestBreakBudget> {
        val matched = filter(items) { candidate: GlobalRestBreakBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRestBreakBudgets(store: GlobalRestBreakBudgetStore, query: GlobalRestBreakBudgetQuery): MutableList<GlobalRestBreakBudget> {
    return query.apply(store.list())
}
