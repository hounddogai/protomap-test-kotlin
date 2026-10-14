package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict appointment summaries by code. */
interface StrictAppointmentSummaryStore {
    fun find(code: String): StrictAppointmentSummary?
    fun save(item: StrictAppointmentSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAppointmentSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAppointmentSummaryStore(
    var items: MutableMap<String, StrictAppointmentSummary>,
    var order: MutableList<String>,
) : StrictAppointmentSummaryStore {
    override fun find(code: String): StrictAppointmentSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAppointmentSummary) {
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

    override fun list(): MutableList<StrictAppointmentSummary> {
        val out = mutableListOf<StrictAppointmentSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAppointmentSummaryStore(): StrictAppointmentSummaryStore {
    return MemoryStrictAppointmentSummaryStore(
        mutableMapOf<String, StrictAppointmentSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAppointmentSummaryStore(
    var inner: StrictAppointmentSummaryStore,
    var cache: TtlCache<StrictAppointmentSummary>,
    var hits: Int,
    var misses: Int,
) : StrictAppointmentSummaryStore {
    override fun find(code: String): StrictAppointmentSummary? {
        val hit = (cache.get(code) as StrictAppointmentSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAppointmentSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAppointmentSummary> = inner.list()
}

fun newCachedStrictAppointmentSummaryStore(inner: StrictAppointmentSummaryStore, ttl: Int, clock: Clock): StrictAppointmentSummaryStore {
    return CachedStrictAppointmentSummaryStore(
        inner = inner,
        cache = newTtlCache<StrictAppointmentSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict appointment summaries. Empty fields match all. */
data class StrictAppointmentSummaryQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAppointmentSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAppointmentSummary>): MutableList<StrictAppointmentSummary> {
        val matched = filter(items) { candidate: StrictAppointmentSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAppointmentSummaries(store: StrictAppointmentSummaryStore, query: StrictAppointmentSummaryQuery): MutableList<StrictAppointmentSummary> {
    return query.apply(store.list())
}
