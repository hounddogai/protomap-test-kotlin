package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary near miss budgets by code. */
interface PrimaryNearMissBudgetStore {
    fun find(code: String): PrimaryNearMissBudget?
    fun save(item: PrimaryNearMissBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNearMissBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNearMissBudgetStore(
    var items: MutableMap<String, PrimaryNearMissBudget>,
    var order: MutableList<String>,
) : PrimaryNearMissBudgetStore {
    override fun find(code: String): PrimaryNearMissBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNearMissBudget) {
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

    override fun list(): MutableList<PrimaryNearMissBudget> {
        val out = mutableListOf<PrimaryNearMissBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNearMissBudgetStore(): PrimaryNearMissBudgetStore {
    return MemoryPrimaryNearMissBudgetStore(
        mutableMapOf<String, PrimaryNearMissBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNearMissBudgetStore(
    var inner: PrimaryNearMissBudgetStore,
    var cache: TtlCache<PrimaryNearMissBudget>,
    var hits: Int,
    var misses: Int,
) : PrimaryNearMissBudgetStore {
    override fun find(code: String): PrimaryNearMissBudget? {
        val hit = (cache.get(code) as PrimaryNearMissBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNearMissBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNearMissBudget> = inner.list()
}

fun newCachedPrimaryNearMissBudgetStore(inner: PrimaryNearMissBudgetStore, ttl: Int, clock: Clock): PrimaryNearMissBudgetStore {
    return CachedPrimaryNearMissBudgetStore(
        inner = inner,
        cache = newTtlCache<PrimaryNearMissBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary near miss budgets. Empty fields match all. */
data class PrimaryNearMissBudgetQuery(
    var category: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNearMissBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryNearMissBudget>): MutableList<PrimaryNearMissBudget> {
        val matched = filter(items) { candidate: PrimaryNearMissBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNearMissBudgets(store: PrimaryNearMissBudgetStore, query: PrimaryNearMissBudgetQuery): MutableList<PrimaryNearMissBudget> {
    return query.apply(store.list())
}
