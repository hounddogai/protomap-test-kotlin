package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching budgets by code. */
interface CoachingBudgetStore {
    fun find(code: String): CoachingBudget?
    fun save(item: CoachingBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingBudgetStore(
    var items: MutableMap<String, CoachingBudget>,
    var order: MutableList<String>,
) : CoachingBudgetStore {
    override fun find(code: String): CoachingBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingBudget) {
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

    override fun list(): MutableList<CoachingBudget> {
        val out = mutableListOf<CoachingBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingBudgetStore(): CoachingBudgetStore {
    return MemoryCoachingBudgetStore(mutableMapOf<String, CoachingBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingBudgetStore(
    var inner: CoachingBudgetStore,
    var cache: TtlCache<CoachingBudget>,
    var hits: Int,
    var misses: Int,
) : CoachingBudgetStore {
    override fun find(code: String): CoachingBudget? {
        val hit = (cache.get(code) as CoachingBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingBudget> = inner.list()
}

fun newCachedCoachingBudgetStore(inner: CoachingBudgetStore, ttl: Int, clock: Clock): CoachingBudgetStore {
    return CachedCoachingBudgetStore(
        inner = inner,
        cache = newTtlCache<CoachingBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching budgets. Empty fields match all. */
data class CoachingBudgetQuery(
    var locale: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingBudget>): MutableList<CoachingBudget> {
        val matched = filter(items) { candidate: CoachingBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingBudgets(store: CoachingBudgetStore, query: CoachingBudgetQuery): MutableList<CoachingBudget> {
    return query.apply(store.list())
}
