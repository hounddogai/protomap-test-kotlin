package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient transit time budgets by code. */
interface LenientTransitTimeBudgetStore {
    fun find(code: String): LenientTransitTimeBudget?
    fun save(item: LenientTransitTimeBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTransitTimeBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTransitTimeBudgetStore(
    var items: MutableMap<String, LenientTransitTimeBudget>,
    var order: MutableList<String>,
) : LenientTransitTimeBudgetStore {
    override fun find(code: String): LenientTransitTimeBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTransitTimeBudget) {
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

    override fun list(): MutableList<LenientTransitTimeBudget> {
        val out = mutableListOf<LenientTransitTimeBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTransitTimeBudgetStore(): LenientTransitTimeBudgetStore {
    return MemoryLenientTransitTimeBudgetStore(
        mutableMapOf<String, LenientTransitTimeBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTransitTimeBudgetStore(
    var inner: LenientTransitTimeBudgetStore,
    var cache: TtlCache<LenientTransitTimeBudget>,
    var hits: Int,
    var misses: Int,
) : LenientTransitTimeBudgetStore {
    override fun find(code: String): LenientTransitTimeBudget? {
        val hit = (cache.get(code) as LenientTransitTimeBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTransitTimeBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTransitTimeBudget> = inner.list()
}

fun newCachedLenientTransitTimeBudgetStore(inner: LenientTransitTimeBudgetStore, ttl: Int, clock: Clock): LenientTransitTimeBudgetStore {
    return CachedLenientTransitTimeBudgetStore(
        inner = inner,
        cache = newTtlCache<LenientTransitTimeBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient transit time budgets. Empty fields match all. */
data class LenientTransitTimeBudgetQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTransitTimeBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientTransitTimeBudget>): MutableList<LenientTransitTimeBudget> {
        val matched = filter(items) { candidate: LenientTransitTimeBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTransitTimeBudgets(store: LenientTransitTimeBudgetStore, query: LenientTransitTimeBudgetQuery): MutableList<LenientTransitTimeBudget> {
    return query.apply(store.list())
}
