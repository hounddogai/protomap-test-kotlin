package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation budgets by code. */
interface RollingViolationBudgetStore {
    fun find(code: String): RollingViolationBudget?
    fun save(item: RollingViolationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationBudgetStore(
    var items: MutableMap<String, RollingViolationBudget>,
    var order: MutableList<String>,
) : RollingViolationBudgetStore {
    override fun find(code: String): RollingViolationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationBudget) {
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

    override fun list(): MutableList<RollingViolationBudget> {
        val out = mutableListOf<RollingViolationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationBudgetStore(): RollingViolationBudgetStore {
    return MemoryRollingViolationBudgetStore(
        mutableMapOf<String, RollingViolationBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationBudgetStore(
    var inner: RollingViolationBudgetStore,
    var cache: TtlCache<RollingViolationBudget>,
    var hits: Int,
    var misses: Int,
) : RollingViolationBudgetStore {
    override fun find(code: String): RollingViolationBudget? {
        val hit = (cache.get(code) as RollingViolationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationBudget> = inner.list()
}

fun newCachedRollingViolationBudgetStore(inner: RollingViolationBudgetStore, ttl: Int, clock: Clock): RollingViolationBudgetStore {
    return CachedRollingViolationBudgetStore(
        inner = inner,
        cache = newTtlCache<RollingViolationBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation budgets. Empty fields match all. */
data class RollingViolationBudgetQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingViolationBudget>): MutableList<RollingViolationBudget> {
        val matched = filter(items) { candidate: RollingViolationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationBudgets(store: RollingViolationBudgetStore, query: RollingViolationBudgetQuery): MutableList<RollingViolationBudget> {
    return query.apply(store.list())
}
