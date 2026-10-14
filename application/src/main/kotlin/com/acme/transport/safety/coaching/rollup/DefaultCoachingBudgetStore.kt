package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching budgets by code. */
interface DefaultCoachingBudgetStore {
    fun find(code: String): DefaultCoachingBudget?
    fun save(item: DefaultCoachingBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingBudgetStore(
    var items: MutableMap<String, DefaultCoachingBudget>,
    var order: MutableList<String>,
) : DefaultCoachingBudgetStore {
    override fun find(code: String): DefaultCoachingBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingBudget) {
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

    override fun list(): MutableList<DefaultCoachingBudget> {
        val out = mutableListOf<DefaultCoachingBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingBudgetStore(): DefaultCoachingBudgetStore {
    return MemoryDefaultCoachingBudgetStore(
        mutableMapOf<String, DefaultCoachingBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingBudgetStore(
    var inner: DefaultCoachingBudgetStore,
    var cache: TtlCache<DefaultCoachingBudget>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingBudgetStore {
    override fun find(code: String): DefaultCoachingBudget? {
        val hit = (cache.get(code) as DefaultCoachingBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingBudget> = inner.list()
}

fun newCachedDefaultCoachingBudgetStore(inner: DefaultCoachingBudgetStore, ttl: Int, clock: Clock): DefaultCoachingBudgetStore {
    return CachedDefaultCoachingBudgetStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching budgets. Empty fields match all. */
data class DefaultCoachingBudgetQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingBudget): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DefaultCoachingBudget>): MutableList<DefaultCoachingBudget> {
        val matched = filter(items) { candidate: DefaultCoachingBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingBudgets(store: DefaultCoachingBudgetStore, query: DefaultCoachingBudgetQuery): MutableList<DefaultCoachingBudget> {
    return query.apply(store.list())
}
