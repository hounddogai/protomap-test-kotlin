package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy relay budgets by code. */
interface LegacyRelayBudgetStore {
    fun find(code: String): LegacyRelayBudget?
    fun save(item: LegacyRelayBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRelayBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRelayBudgetStore(
    var items: MutableMap<String, LegacyRelayBudget>,
    var order: MutableList<String>,
) : LegacyRelayBudgetStore {
    override fun find(code: String): LegacyRelayBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRelayBudget) {
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

    override fun list(): MutableList<LegacyRelayBudget> {
        val out = mutableListOf<LegacyRelayBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRelayBudgetStore(): LegacyRelayBudgetStore {
    return MemoryLegacyRelayBudgetStore(mutableMapOf<String, LegacyRelayBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRelayBudgetStore(
    var inner: LegacyRelayBudgetStore,
    var cache: TtlCache<LegacyRelayBudget>,
    var hits: Int,
    var misses: Int,
) : LegacyRelayBudgetStore {
    override fun find(code: String): LegacyRelayBudget? {
        val hit = (cache.get(code) as LegacyRelayBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRelayBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRelayBudget> = inner.list()
}

fun newCachedLegacyRelayBudgetStore(inner: LegacyRelayBudgetStore, ttl: Int, clock: Clock): LegacyRelayBudgetStore {
    return CachedLegacyRelayBudgetStore(
        inner = inner,
        cache = newTtlCache<LegacyRelayBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy relay budgets. Empty fields match all. */
data class LegacyRelayBudgetQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRelayBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRelayBudget>): MutableList<LegacyRelayBudget> {
        val matched = filter(items) { candidate: LegacyRelayBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRelayBudgets(store: LegacyRelayBudgetStore, query: LegacyRelayBudgetQuery): MutableList<LegacyRelayBudget> {
    return query.apply(store.list())
}
