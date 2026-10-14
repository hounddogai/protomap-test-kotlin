package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire budgets by code. */
interface TireBudgetStore {
    fun find(code: String): TireBudget?
    fun save(item: TireBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireBudgetStore(
    var items: MutableMap<String, TireBudget>,
    var order: MutableList<String>,
) : TireBudgetStore {
    override fun find(code: String): TireBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireBudget) {
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

    override fun list(): MutableList<TireBudget> {
        val out = mutableListOf<TireBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireBudgetStore(): TireBudgetStore {
    return MemoryTireBudgetStore(mutableMapOf<String, TireBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireBudgetStore(
    var inner: TireBudgetStore,
    var cache: TtlCache<TireBudget>,
    var hits: Int,
    var misses: Int,
) : TireBudgetStore {
    override fun find(code: String): TireBudget? {
        val hit = (cache.get(code) as TireBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireBudget> = inner.list()
}

fun newCachedTireBudgetStore(inner: TireBudgetStore, ttl: Int, clock: Clock): TireBudgetStore {
    return CachedTireBudgetStore(
        inner = inner,
        cache = newTtlCache<TireBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire budgets. Empty fields match all. */
data class TireBudgetQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireBudget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireBudget>): MutableList<TireBudget> {
        val matched = filter(items) { candidate: TireBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireBudgets(store: TireBudgetStore, query: TireBudgetQuery): MutableList<TireBudget> {
    return query.apply(store.list())
}
