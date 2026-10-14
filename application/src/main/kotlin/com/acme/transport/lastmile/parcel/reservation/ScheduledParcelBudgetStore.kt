package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled parcel budgets by code. */
interface ScheduledParcelBudgetStore {
    fun find(code: String): ScheduledParcelBudget?
    fun save(item: ScheduledParcelBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledParcelBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledParcelBudgetStore(
    var items: MutableMap<String, ScheduledParcelBudget>,
    var order: MutableList<String>,
) : ScheduledParcelBudgetStore {
    override fun find(code: String): ScheduledParcelBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledParcelBudget) {
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

    override fun list(): MutableList<ScheduledParcelBudget> {
        val out = mutableListOf<ScheduledParcelBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledParcelBudgetStore(): ScheduledParcelBudgetStore {
    return MemoryScheduledParcelBudgetStore(
        mutableMapOf<String, ScheduledParcelBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledParcelBudgetStore(
    var inner: ScheduledParcelBudgetStore,
    var cache: TtlCache<ScheduledParcelBudget>,
    var hits: Int,
    var misses: Int,
) : ScheduledParcelBudgetStore {
    override fun find(code: String): ScheduledParcelBudget? {
        val hit = (cache.get(code) as ScheduledParcelBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledParcelBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledParcelBudget> = inner.list()
}

fun newCachedScheduledParcelBudgetStore(inner: ScheduledParcelBudgetStore, ttl: Int, clock: Clock): ScheduledParcelBudgetStore {
    return CachedScheduledParcelBudgetStore(
        inner = inner,
        cache = newTtlCache<ScheduledParcelBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled parcel budgets. Empty fields match all. */
data class ScheduledParcelBudgetQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledParcelBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledParcelBudget>): MutableList<ScheduledParcelBudget> {
        val matched = filter(items) { candidate: ScheduledParcelBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledParcelBudgets(store: ScheduledParcelBudgetStore, query: ScheduledParcelBudgetQuery): MutableList<ScheduledParcelBudget> {
    return query.apply(store.list())
}
