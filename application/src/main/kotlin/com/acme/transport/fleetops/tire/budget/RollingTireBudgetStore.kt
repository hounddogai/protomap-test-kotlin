package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling tire budgets by code. */
interface RollingTireBudgetStore {
    fun find(code: String): RollingTireBudget?
    fun save(item: RollingTireBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTireBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTireBudgetStore(
    var items: MutableMap<String, RollingTireBudget>,
    var order: MutableList<String>,
) : RollingTireBudgetStore {
    override fun find(code: String): RollingTireBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTireBudget) {
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

    override fun list(): MutableList<RollingTireBudget> {
        val out = mutableListOf<RollingTireBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTireBudgetStore(): RollingTireBudgetStore {
    return MemoryRollingTireBudgetStore(mutableMapOf<String, RollingTireBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTireBudgetStore(
    var inner: RollingTireBudgetStore,
    var cache: TtlCache<RollingTireBudget>,
    var hits: Int,
    var misses: Int,
) : RollingTireBudgetStore {
    override fun find(code: String): RollingTireBudget? {
        val hit = (cache.get(code) as RollingTireBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTireBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTireBudget> = inner.list()
}

fun newCachedRollingTireBudgetStore(inner: RollingTireBudgetStore, ttl: Int, clock: Clock): RollingTireBudgetStore {
    return CachedRollingTireBudgetStore(
        inner = inner,
        cache = newTtlCache<RollingTireBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tire budgets. Empty fields match all. */
data class RollingTireBudgetQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTireBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTireBudget>): MutableList<RollingTireBudget> {
        val matched = filter(items) { candidate: RollingTireBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTireBudgets(store: RollingTireBudgetStore, query: RollingTireBudgetQuery): MutableList<RollingTireBudget> {
    return query.apply(store.list())
}
