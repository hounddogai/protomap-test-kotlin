package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered transit time budgets by code. */
interface TieredTransitTimeBudgetStore {
    fun find(code: String): TieredTransitTimeBudget?
    fun save(item: TieredTransitTimeBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTransitTimeBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTransitTimeBudgetStore(
    var items: MutableMap<String, TieredTransitTimeBudget>,
    var order: MutableList<String>,
) : TieredTransitTimeBudgetStore {
    override fun find(code: String): TieredTransitTimeBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTransitTimeBudget) {
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

    override fun list(): MutableList<TieredTransitTimeBudget> {
        val out = mutableListOf<TieredTransitTimeBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTransitTimeBudgetStore(): TieredTransitTimeBudgetStore {
    return MemoryTieredTransitTimeBudgetStore(
        mutableMapOf<String, TieredTransitTimeBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTransitTimeBudgetStore(
    var inner: TieredTransitTimeBudgetStore,
    var cache: TtlCache<TieredTransitTimeBudget>,
    var hits: Int,
    var misses: Int,
) : TieredTransitTimeBudgetStore {
    override fun find(code: String): TieredTransitTimeBudget? {
        val hit = (cache.get(code) as TieredTransitTimeBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTransitTimeBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTransitTimeBudget> = inner.list()
}

fun newCachedTieredTransitTimeBudgetStore(inner: TieredTransitTimeBudgetStore, ttl: Int, clock: Clock): TieredTransitTimeBudgetStore {
    return CachedTieredTransitTimeBudgetStore(
        inner = inner,
        cache = newTtlCache<TieredTransitTimeBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered transit time budgets. Empty fields match all. */
data class TieredTransitTimeBudgetQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTransitTimeBudget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTransitTimeBudget>): MutableList<TieredTransitTimeBudget> {
        val matched = filter(items) { candidate: TieredTransitTimeBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTransitTimeBudgets(store: TieredTransitTimeBudgetStore, query: TieredTransitTimeBudgetQuery): MutableList<TieredTransitTimeBudget> {
    return query.apply(store.list())
}
