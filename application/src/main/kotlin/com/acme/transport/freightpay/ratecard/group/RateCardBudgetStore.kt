package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card budgets by code. */
interface RateCardBudgetStore {
    fun find(code: String): RateCardBudget?
    fun save(item: RateCardBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardBudgetStore(
    var items: MutableMap<String, RateCardBudget>,
    var order: MutableList<String>,
) : RateCardBudgetStore {
    override fun find(code: String): RateCardBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardBudget) {
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

    override fun list(): MutableList<RateCardBudget> {
        val out = mutableListOf<RateCardBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardBudgetStore(): RateCardBudgetStore {
    return MemoryRateCardBudgetStore(mutableMapOf<String, RateCardBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardBudgetStore(
    var inner: RateCardBudgetStore,
    var cache: TtlCache<RateCardBudget>,
    var hits: Int,
    var misses: Int,
) : RateCardBudgetStore {
    override fun find(code: String): RateCardBudget? {
        val hit = (cache.get(code) as RateCardBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardBudget> = inner.list()
}

fun newCachedRateCardBudgetStore(inner: RateCardBudgetStore, ttl: Int, clock: Clock): RateCardBudgetStore {
    return CachedRateCardBudgetStore(
        inner = inner,
        cache = newTtlCache<RateCardBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card budgets. Empty fields match all. */
data class RateCardBudgetQuery(
    var segment: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardBudget>): MutableList<RateCardBudget> {
        val matched = filter(items) { candidate: RateCardBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardBudgets(store: RateCardBudgetStore, query: RateCardBudgetQuery): MutableList<RateCardBudget> {
    return query.apply(store.list())
}
