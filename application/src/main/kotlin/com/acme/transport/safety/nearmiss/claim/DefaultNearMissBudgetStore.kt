package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default near miss budgets by code. */
interface DefaultNearMissBudgetStore {
    fun find(code: String): DefaultNearMissBudget?
    fun save(item: DefaultNearMissBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultNearMissBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultNearMissBudgetStore(
    var items: MutableMap<String, DefaultNearMissBudget>,
    var order: MutableList<String>,
) : DefaultNearMissBudgetStore {
    override fun find(code: String): DefaultNearMissBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultNearMissBudget) {
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

    override fun list(): MutableList<DefaultNearMissBudget> {
        val out = mutableListOf<DefaultNearMissBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultNearMissBudgetStore(): DefaultNearMissBudgetStore {
    return MemoryDefaultNearMissBudgetStore(
        mutableMapOf<String, DefaultNearMissBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultNearMissBudgetStore(
    var inner: DefaultNearMissBudgetStore,
    var cache: TtlCache<DefaultNearMissBudget>,
    var hits: Int,
    var misses: Int,
) : DefaultNearMissBudgetStore {
    override fun find(code: String): DefaultNearMissBudget? {
        val hit = (cache.get(code) as DefaultNearMissBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultNearMissBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultNearMissBudget> = inner.list()
}

fun newCachedDefaultNearMissBudgetStore(inner: DefaultNearMissBudgetStore, ttl: Int, clock: Clock): DefaultNearMissBudgetStore {
    return CachedDefaultNearMissBudgetStore(
        inner = inner,
        cache = newTtlCache<DefaultNearMissBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default near miss budgets. Empty fields match all. */
data class DefaultNearMissBudgetQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultNearMissBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DefaultNearMissBudget>): MutableList<DefaultNearMissBudget> {
        val matched = filter(items) { candidate: DefaultNearMissBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultNearMissBudgets(store: DefaultNearMissBudgetStore, query: DefaultNearMissBudgetQuery): MutableList<DefaultNearMissBudget> {
    return query.apply(store.list())
}
