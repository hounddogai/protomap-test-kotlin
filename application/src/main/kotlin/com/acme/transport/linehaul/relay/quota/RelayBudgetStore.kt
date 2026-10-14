package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay budgets by code. */
interface RelayBudgetStore {
    fun find(code: String): RelayBudget?
    fun save(item: RelayBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayBudgetStore(
    var items: MutableMap<String, RelayBudget>,
    var order: MutableList<String>,
) : RelayBudgetStore {
    override fun find(code: String): RelayBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayBudget) {
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

    override fun list(): MutableList<RelayBudget> {
        val out = mutableListOf<RelayBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayBudgetStore(): RelayBudgetStore {
    return MemoryRelayBudgetStore(mutableMapOf<String, RelayBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayBudgetStore(
    var inner: RelayBudgetStore,
    var cache: TtlCache<RelayBudget>,
    var hits: Int,
    var misses: Int,
) : RelayBudgetStore {
    override fun find(code: String): RelayBudget? {
        val hit = (cache.get(code) as RelayBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayBudget> = inner.list()
}

fun newCachedRelayBudgetStore(inner: RelayBudgetStore, ttl: Int, clock: Clock): RelayBudgetStore {
    return CachedRelayBudgetStore(
        inner = inner,
        cache = newTtlCache<RelayBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay budgets. Empty fields match all. */
data class RelayBudgetQuery(
    var region: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayBudget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayBudget>): MutableList<RelayBudget> {
        val matched = filter(items) { candidate: RelayBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayBudgets(store: RelayBudgetStore, query: RelayBudgetQuery): MutableList<RelayBudget> {
    return query.apply(store.list())
}
