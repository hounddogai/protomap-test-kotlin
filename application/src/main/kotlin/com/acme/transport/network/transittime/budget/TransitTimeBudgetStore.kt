package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time budgets by code. */
interface TransitTimeBudgetStore {
    fun find(code: String): TransitTimeBudget?
    fun save(item: TransitTimeBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeBudgetStore(
    var items: MutableMap<String, TransitTimeBudget>,
    var order: MutableList<String>,
) : TransitTimeBudgetStore {
    override fun find(code: String): TransitTimeBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeBudget) {
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

    override fun list(): MutableList<TransitTimeBudget> {
        val out = mutableListOf<TransitTimeBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeBudgetStore(): TransitTimeBudgetStore {
    return MemoryTransitTimeBudgetStore(mutableMapOf<String, TransitTimeBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeBudgetStore(
    var inner: TransitTimeBudgetStore,
    var cache: TtlCache<TransitTimeBudget>,
    var hits: Int,
    var misses: Int,
) : TransitTimeBudgetStore {
    override fun find(code: String): TransitTimeBudget? {
        val hit = (cache.get(code) as TransitTimeBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeBudget> = inner.list()
}

fun newCachedTransitTimeBudgetStore(inner: TransitTimeBudgetStore, ttl: Int, clock: Clock): TransitTimeBudgetStore {
    return CachedTransitTimeBudgetStore(
        inner = inner,
        cache = newTtlCache<TransitTimeBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time budgets. Empty fields match all. */
data class TransitTimeBudgetQuery(
    var unit: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeBudget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeBudget>): MutableList<TransitTimeBudget> {
        val matched = filter(items) { candidate: TransitTimeBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeBudgets(store: TransitTimeBudgetStore, query: TransitTimeBudgetQuery): MutableList<TransitTimeBudget> {
    return query.apply(store.list())
}
