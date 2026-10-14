package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker budgets by code. */
interface LockerBudgetStore {
    fun find(code: String): LockerBudget?
    fun save(item: LockerBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerBudgetStore(
    var items: MutableMap<String, LockerBudget>,
    var order: MutableList<String>,
) : LockerBudgetStore {
    override fun find(code: String): LockerBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerBudget) {
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

    override fun list(): MutableList<LockerBudget> {
        val out = mutableListOf<LockerBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerBudgetStore(): LockerBudgetStore {
    return MemoryLockerBudgetStore(mutableMapOf<String, LockerBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerBudgetStore(
    var inner: LockerBudgetStore,
    var cache: TtlCache<LockerBudget>,
    var hits: Int,
    var misses: Int,
) : LockerBudgetStore {
    override fun find(code: String): LockerBudget? {
        val hit = (cache.get(code) as LockerBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerBudget> = inner.list()
}

fun newCachedLockerBudgetStore(inner: LockerBudgetStore, ttl: Int, clock: Clock): LockerBudgetStore {
    return CachedLockerBudgetStore(
        inner = inner,
        cache = newTtlCache<LockerBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker budgets. Empty fields match all. */
data class LockerBudgetQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerBudget>): MutableList<LockerBudget> {
        val matched = filter(items) { candidate: LockerBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerBudgets(store: LockerBudgetStore, query: LockerBudgetQuery): MutableList<LockerBudget> {
    return query.apply(store.list())
}
