package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time budgets by code. */
interface PrimaryTransitTimeBudgetStore {
    fun find(code: String): PrimaryTransitTimeBudget?
    fun save(item: PrimaryTransitTimeBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeBudgetStore(
    var items: MutableMap<String, PrimaryTransitTimeBudget>,
    var order: MutableList<String>,
) : PrimaryTransitTimeBudgetStore {
    override fun find(code: String): PrimaryTransitTimeBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeBudget) {
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

    override fun list(): MutableList<PrimaryTransitTimeBudget> {
        val out = mutableListOf<PrimaryTransitTimeBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeBudgetStore(): PrimaryTransitTimeBudgetStore {
    return MemoryPrimaryTransitTimeBudgetStore(
        mutableMapOf<String, PrimaryTransitTimeBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeBudgetStore(
    var inner: PrimaryTransitTimeBudgetStore,
    var cache: TtlCache<PrimaryTransitTimeBudget>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeBudgetStore {
    override fun find(code: String): PrimaryTransitTimeBudget? {
        val hit = (cache.get(code) as PrimaryTransitTimeBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeBudget> = inner.list()
}

fun newCachedPrimaryTransitTimeBudgetStore(inner: PrimaryTransitTimeBudgetStore, ttl: Int, clock: Clock): PrimaryTransitTimeBudgetStore {
    return CachedPrimaryTransitTimeBudgetStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time budgets. Empty fields match all. */
data class PrimaryTransitTimeBudgetQuery(
    var depot: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeBudget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTransitTimeBudget>): MutableList<PrimaryTransitTimeBudget> {
        val matched = filter(items) { candidate: PrimaryTransitTimeBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeBudgets(store: PrimaryTransitTimeBudgetStore, query: PrimaryTransitTimeBudgetQuery): MutableList<PrimaryTransitTimeBudget> {
    return query.apply(store.list())
}
