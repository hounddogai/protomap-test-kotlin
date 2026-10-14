package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched appointment budgets by code. */
interface BatchedAppointmentBudgetStore {
    fun find(code: String): BatchedAppointmentBudget?
    fun save(item: BatchedAppointmentBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAppointmentBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAppointmentBudgetStore(
    var items: MutableMap<String, BatchedAppointmentBudget>,
    var order: MutableList<String>,
) : BatchedAppointmentBudgetStore {
    override fun find(code: String): BatchedAppointmentBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAppointmentBudget) {
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

    override fun list(): MutableList<BatchedAppointmentBudget> {
        val out = mutableListOf<BatchedAppointmentBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAppointmentBudgetStore(): BatchedAppointmentBudgetStore {
    return MemoryBatchedAppointmentBudgetStore(
        mutableMapOf<String, BatchedAppointmentBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAppointmentBudgetStore(
    var inner: BatchedAppointmentBudgetStore,
    var cache: TtlCache<BatchedAppointmentBudget>,
    var hits: Int,
    var misses: Int,
) : BatchedAppointmentBudgetStore {
    override fun find(code: String): BatchedAppointmentBudget? {
        val hit = (cache.get(code) as BatchedAppointmentBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAppointmentBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAppointmentBudget> = inner.list()
}

fun newCachedBatchedAppointmentBudgetStore(inner: BatchedAppointmentBudgetStore, ttl: Int, clock: Clock): BatchedAppointmentBudgetStore {
    return CachedBatchedAppointmentBudgetStore(
        inner = inner,
        cache = newTtlCache<BatchedAppointmentBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched appointment budgets. Empty fields match all. */
data class BatchedAppointmentBudgetQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAppointmentBudget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAppointmentBudget>): MutableList<BatchedAppointmentBudget> {
        val matched = filter(items) { candidate: BatchedAppointmentBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAppointmentBudgets(store: BatchedAppointmentBudgetStore, query: BatchedAppointmentBudgetQuery): MutableList<BatchedAppointmentBudget> {
    return query.apply(store.list())
}
