package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss budgets by code. */
interface NearMissBudgetStore {
    fun find(code: String): NearMissBudget?
    fun save(item: NearMissBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissBudgetStore(
    var items: MutableMap<String, NearMissBudget>,
    var order: MutableList<String>,
) : NearMissBudgetStore {
    override fun find(code: String): NearMissBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissBudget) {
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

    override fun list(): MutableList<NearMissBudget> {
        val out = mutableListOf<NearMissBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissBudgetStore(): NearMissBudgetStore {
    return MemoryNearMissBudgetStore(mutableMapOf<String, NearMissBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissBudgetStore(
    var inner: NearMissBudgetStore,
    var cache: TtlCache<NearMissBudget>,
    var hits: Int,
    var misses: Int,
) : NearMissBudgetStore {
    override fun find(code: String): NearMissBudget? {
        val hit = (cache.get(code) as NearMissBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissBudget> = inner.list()
}

fun newCachedNearMissBudgetStore(inner: NearMissBudgetStore, ttl: Int, clock: Clock): NearMissBudgetStore {
    return CachedNearMissBudgetStore(
        inner = inner,
        cache = newTtlCache<NearMissBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss budgets. Empty fields match all. */
data class NearMissBudgetQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissBudget>): MutableList<NearMissBudget> {
        val matched = filter(items) { candidate: NearMissBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissBudgets(store: NearMissBudgetStore, query: NearMissBudgetQuery): MutableList<NearMissBudget> {
    return query.apply(store.list())
}
