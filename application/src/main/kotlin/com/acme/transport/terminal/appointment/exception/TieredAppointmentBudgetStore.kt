package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered appointment budgets by code. */
interface TieredAppointmentBudgetStore {
    fun find(code: String): TieredAppointmentBudget?
    fun save(item: TieredAppointmentBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAppointmentBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAppointmentBudgetStore(
    var items: MutableMap<String, TieredAppointmentBudget>,
    var order: MutableList<String>,
) : TieredAppointmentBudgetStore {
    override fun find(code: String): TieredAppointmentBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAppointmentBudget) {
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

    override fun list(): MutableList<TieredAppointmentBudget> {
        val out = mutableListOf<TieredAppointmentBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAppointmentBudgetStore(): TieredAppointmentBudgetStore {
    return MemoryTieredAppointmentBudgetStore(
        mutableMapOf<String, TieredAppointmentBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAppointmentBudgetStore(
    var inner: TieredAppointmentBudgetStore,
    var cache: TtlCache<TieredAppointmentBudget>,
    var hits: Int,
    var misses: Int,
) : TieredAppointmentBudgetStore {
    override fun find(code: String): TieredAppointmentBudget? {
        val hit = (cache.get(code) as TieredAppointmentBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAppointmentBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAppointmentBudget> = inner.list()
}

fun newCachedTieredAppointmentBudgetStore(inner: TieredAppointmentBudgetStore, ttl: Int, clock: Clock): TieredAppointmentBudgetStore {
    return CachedTieredAppointmentBudgetStore(
        inner = inner,
        cache = newTtlCache<TieredAppointmentBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered appointment budgets. Empty fields match all. */
data class TieredAppointmentBudgetQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAppointmentBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAppointmentBudget>): MutableList<TieredAppointmentBudget> {
        val matched = filter(items) { candidate: TieredAppointmentBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAppointmentBudgets(store: TieredAppointmentBudgetStore, query: TieredAppointmentBudgetQuery): MutableList<TieredAppointmentBudget> {
    return query.apply(store.list())
}
