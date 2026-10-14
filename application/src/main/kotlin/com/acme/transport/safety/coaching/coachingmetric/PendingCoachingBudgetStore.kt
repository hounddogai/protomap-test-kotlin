package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending coaching budgets by code. */
interface PendingCoachingBudgetStore {
    fun find(code: String): PendingCoachingBudget?
    fun save(item: PendingCoachingBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCoachingBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCoachingBudgetStore(
    var items: MutableMap<String, PendingCoachingBudget>,
    var order: MutableList<String>,
) : PendingCoachingBudgetStore {
    override fun find(code: String): PendingCoachingBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCoachingBudget) {
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

    override fun list(): MutableList<PendingCoachingBudget> {
        val out = mutableListOf<PendingCoachingBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCoachingBudgetStore(): PendingCoachingBudgetStore {
    return MemoryPendingCoachingBudgetStore(
        mutableMapOf<String, PendingCoachingBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCoachingBudgetStore(
    var inner: PendingCoachingBudgetStore,
    var cache: TtlCache<PendingCoachingBudget>,
    var hits: Int,
    var misses: Int,
) : PendingCoachingBudgetStore {
    override fun find(code: String): PendingCoachingBudget? {
        val hit = (cache.get(code) as PendingCoachingBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCoachingBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCoachingBudget> = inner.list()
}

fun newCachedPendingCoachingBudgetStore(inner: PendingCoachingBudgetStore, ttl: Int, clock: Clock): PendingCoachingBudgetStore {
    return CachedPendingCoachingBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingCoachingBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending coaching budgets. Empty fields match all. */
data class PendingCoachingBudgetQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCoachingBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<PendingCoachingBudget>): MutableList<PendingCoachingBudget> {
        val matched = filter(items) { candidate: PendingCoachingBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCoachingBudgets(store: PendingCoachingBudgetStore, query: PendingCoachingBudgetQuery): MutableList<PendingCoachingBudget> {
    return query.apply(store.list())
}
