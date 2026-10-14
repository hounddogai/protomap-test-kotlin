package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub budgets by code. */
interface HubBudgetStore {
    fun find(code: String): HubBudget?
    fun save(item: HubBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubBudgetStore(
    var items: MutableMap<String, HubBudget>,
    var order: MutableList<String>,
) : HubBudgetStore {
    override fun find(code: String): HubBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubBudget) {
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

    override fun list(): MutableList<HubBudget> {
        val out = mutableListOf<HubBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubBudgetStore(): HubBudgetStore {
    return MemoryHubBudgetStore(mutableMapOf<String, HubBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubBudgetStore(
    var inner: HubBudgetStore,
    var cache: TtlCache<HubBudget>,
    var hits: Int,
    var misses: Int,
) : HubBudgetStore {
    override fun find(code: String): HubBudget? {
        val hit = (cache.get(code) as HubBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubBudget> = inner.list()
}

fun newCachedHubBudgetStore(inner: HubBudgetStore, ttl: Int, clock: Clock): HubBudgetStore {
    return CachedHubBudgetStore(
        inner = inner,
        cache = newTtlCache<HubBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub budgets. Empty fields match all. */
data class HubBudgetQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubBudget>): MutableList<HubBudget> {
        val matched = filter(items) { candidate: HubBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubBudgets(store: HubBudgetStore, query: HubBudgetQuery): MutableList<HubBudget> {
    return query.apply(store.list())
}
