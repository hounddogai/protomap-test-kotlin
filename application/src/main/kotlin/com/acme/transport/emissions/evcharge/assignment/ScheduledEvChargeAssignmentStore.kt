package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled ev charge assignments by code. */
interface ScheduledEvChargeAssignmentStore {
    fun find(code: String): ScheduledEvChargeAssignment?
    fun save(item: ScheduledEvChargeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledEvChargeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledEvChargeAssignmentStore(
    var items: MutableMap<String, ScheduledEvChargeAssignment>,
    var order: MutableList<String>,
) : ScheduledEvChargeAssignmentStore {
    override fun find(code: String): ScheduledEvChargeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledEvChargeAssignment) {
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

    override fun list(): MutableList<ScheduledEvChargeAssignment> {
        val out = mutableListOf<ScheduledEvChargeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledEvChargeAssignmentStore(): ScheduledEvChargeAssignmentStore {
    return MemoryScheduledEvChargeAssignmentStore(
        mutableMapOf<String, ScheduledEvChargeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledEvChargeAssignmentStore(
    var inner: ScheduledEvChargeAssignmentStore,
    var cache: TtlCache<ScheduledEvChargeAssignment>,
    var hits: Int,
    var misses: Int,
) : ScheduledEvChargeAssignmentStore {
    override fun find(code: String): ScheduledEvChargeAssignment? {
        val hit = (cache.get(code) as ScheduledEvChargeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledEvChargeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledEvChargeAssignment> = inner.list()
}

fun newCachedScheduledEvChargeAssignmentStore(inner: ScheduledEvChargeAssignmentStore, ttl: Int, clock: Clock): ScheduledEvChargeAssignmentStore {
    return CachedScheduledEvChargeAssignmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledEvChargeAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled ev charge assignments. Empty fields match all. */
data class ScheduledEvChargeAssignmentQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledEvChargeAssignment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledEvChargeAssignment>): MutableList<ScheduledEvChargeAssignment> {
        val matched = filter(items) { candidate: ScheduledEvChargeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledEvChargeAssignments(store: ScheduledEvChargeAssignmentStore, query: ScheduledEvChargeAssignmentQuery): MutableList<ScheduledEvChargeAssignment> {
    return query.apply(store.list())
}
