package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive locker budgets by code. */
interface AdaptiveLockerBudgetStore {
    fun find(code: String): AdaptiveLockerBudget?
    fun save(item: AdaptiveLockerBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLockerBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLockerBudgetStore(
    var items: MutableMap<String, AdaptiveLockerBudget>,
    var order: MutableList<String>,
) : AdaptiveLockerBudgetStore {
    override fun find(code: String): AdaptiveLockerBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLockerBudget) {
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

    override fun list(): MutableList<AdaptiveLockerBudget> {
        val out = mutableListOf<AdaptiveLockerBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLockerBudgetStore(): AdaptiveLockerBudgetStore {
    return MemoryAdaptiveLockerBudgetStore(
        mutableMapOf<String, AdaptiveLockerBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLockerBudgetStore(
    var inner: AdaptiveLockerBudgetStore,
    var cache: TtlCache<AdaptiveLockerBudget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLockerBudgetStore {
    override fun find(code: String): AdaptiveLockerBudget? {
        val hit = (cache.get(code) as AdaptiveLockerBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLockerBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLockerBudget> = inner.list()
}

fun newCachedAdaptiveLockerBudgetStore(inner: AdaptiveLockerBudgetStore, ttl: Int, clock: Clock): AdaptiveLockerBudgetStore {
    return CachedAdaptiveLockerBudgetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLockerBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive locker budgets. Empty fields match all. */
data class AdaptiveLockerBudgetQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLockerBudget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLockerBudget>): MutableList<AdaptiveLockerBudget> {
        val matched = filter(items) { candidate: AdaptiveLockerBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLockerBudgets(store: AdaptiveLockerBudgetStore, query: AdaptiveLockerBudgetQuery): MutableList<AdaptiveLockerBudget> {
    return query.apply(store.list())
}
