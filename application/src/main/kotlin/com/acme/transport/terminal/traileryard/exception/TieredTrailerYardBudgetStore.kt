package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered trailer yard budgets by code. */
interface TieredTrailerYardBudgetStore {
    fun find(code: String): TieredTrailerYardBudget?
    fun save(item: TieredTrailerYardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTrailerYardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTrailerYardBudgetStore(
    var items: MutableMap<String, TieredTrailerYardBudget>,
    var order: MutableList<String>,
) : TieredTrailerYardBudgetStore {
    override fun find(code: String): TieredTrailerYardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTrailerYardBudget) {
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

    override fun list(): MutableList<TieredTrailerYardBudget> {
        val out = mutableListOf<TieredTrailerYardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTrailerYardBudgetStore(): TieredTrailerYardBudgetStore {
    return MemoryTieredTrailerYardBudgetStore(
        mutableMapOf<String, TieredTrailerYardBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTrailerYardBudgetStore(
    var inner: TieredTrailerYardBudgetStore,
    var cache: TtlCache<TieredTrailerYardBudget>,
    var hits: Int,
    var misses: Int,
) : TieredTrailerYardBudgetStore {
    override fun find(code: String): TieredTrailerYardBudget? {
        val hit = (cache.get(code) as TieredTrailerYardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTrailerYardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTrailerYardBudget> = inner.list()
}

fun newCachedTieredTrailerYardBudgetStore(inner: TieredTrailerYardBudgetStore, ttl: Int, clock: Clock): TieredTrailerYardBudgetStore {
    return CachedTieredTrailerYardBudgetStore(
        inner = inner,
        cache = newTtlCache<TieredTrailerYardBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered trailer yard budgets. Empty fields match all. */
data class TieredTrailerYardBudgetQuery(
    var segment: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTrailerYardBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTrailerYardBudget>): MutableList<TieredTrailerYardBudget> {
        val matched = filter(items) { candidate: TieredTrailerYardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTrailerYardBudgets(store: TieredTrailerYardBudgetStore, query: TieredTrailerYardBudgetQuery): MutableList<TieredTrailerYardBudget> {
    return query.apply(store.list())
}
