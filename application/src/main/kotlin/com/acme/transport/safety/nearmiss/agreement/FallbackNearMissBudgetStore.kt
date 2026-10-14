package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback near miss budgets by code. */
interface FallbackNearMissBudgetStore {
    fun find(code: String): FallbackNearMissBudget?
    fun save(item: FallbackNearMissBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackNearMissBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackNearMissBudgetStore(
    var items: MutableMap<String, FallbackNearMissBudget>,
    var order: MutableList<String>,
) : FallbackNearMissBudgetStore {
    override fun find(code: String): FallbackNearMissBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackNearMissBudget) {
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

    override fun list(): MutableList<FallbackNearMissBudget> {
        val out = mutableListOf<FallbackNearMissBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackNearMissBudgetStore(): FallbackNearMissBudgetStore {
    return MemoryFallbackNearMissBudgetStore(
        mutableMapOf<String, FallbackNearMissBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackNearMissBudgetStore(
    var inner: FallbackNearMissBudgetStore,
    var cache: TtlCache<FallbackNearMissBudget>,
    var hits: Int,
    var misses: Int,
) : FallbackNearMissBudgetStore {
    override fun find(code: String): FallbackNearMissBudget? {
        val hit = (cache.get(code) as FallbackNearMissBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackNearMissBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackNearMissBudget> = inner.list()
}

fun newCachedFallbackNearMissBudgetStore(inner: FallbackNearMissBudgetStore, ttl: Int, clock: Clock): FallbackNearMissBudgetStore {
    return CachedFallbackNearMissBudgetStore(
        inner = inner,
        cache = newTtlCache<FallbackNearMissBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback near miss budgets. Empty fields match all. */
data class FallbackNearMissBudgetQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackNearMissBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackNearMissBudget>): MutableList<FallbackNearMissBudget> {
        val matched = filter(items) { candidate: FallbackNearMissBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackNearMissBudgets(store: FallbackNearMissBudgetStore, query: FallbackNearMissBudgetQuery): MutableList<FallbackNearMissBudget> {
    return query.apply(store.list())
}
