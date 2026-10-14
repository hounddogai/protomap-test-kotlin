package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment budgets by code. */
interface AppointmentBudgetStore {
    fun find(code: String): AppointmentBudget?
    fun save(item: AppointmentBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentBudgetStore(
    var items: MutableMap<String, AppointmentBudget>,
    var order: MutableList<String>,
) : AppointmentBudgetStore {
    override fun find(code: String): AppointmentBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentBudget) {
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

    override fun list(): MutableList<AppointmentBudget> {
        val out = mutableListOf<AppointmentBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentBudgetStore(): AppointmentBudgetStore {
    return MemoryAppointmentBudgetStore(mutableMapOf<String, AppointmentBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentBudgetStore(
    var inner: AppointmentBudgetStore,
    var cache: TtlCache<AppointmentBudget>,
    var hits: Int,
    var misses: Int,
) : AppointmentBudgetStore {
    override fun find(code: String): AppointmentBudget? {
        val hit = (cache.get(code) as AppointmentBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentBudget> = inner.list()
}

fun newCachedAppointmentBudgetStore(inner: AppointmentBudgetStore, ttl: Int, clock: Clock): AppointmentBudgetStore {
    return CachedAppointmentBudgetStore(
        inner = inner,
        cache = newTtlCache<AppointmentBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment budgets. Empty fields match all. */
data class AppointmentBudgetQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentBudget): Boolean {
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

    fun apply(items: MutableList<AppointmentBudget>): MutableList<AppointmentBudget> {
        val matched = filter(items) { candidate: AppointmentBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentBudgets(store: AppointmentBudgetStore, query: AppointmentBudgetQuery): MutableList<AppointmentBudget> {
    return query.apply(store.list())
}
