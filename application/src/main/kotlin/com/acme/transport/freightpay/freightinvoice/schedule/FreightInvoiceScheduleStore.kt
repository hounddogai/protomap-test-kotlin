package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice schedules by code. */
interface FreightInvoiceScheduleStore {
    fun find(code: String): FreightInvoiceSchedule?
    fun save(item: FreightInvoiceSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceScheduleStore(
    var items: MutableMap<String, FreightInvoiceSchedule>,
    var order: MutableList<String>,
) : FreightInvoiceScheduleStore {
    override fun find(code: String): FreightInvoiceSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceSchedule) {
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

    override fun list(): MutableList<FreightInvoiceSchedule> {
        val out = mutableListOf<FreightInvoiceSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceScheduleStore(): FreightInvoiceScheduleStore {
    return MemoryFreightInvoiceScheduleStore(
        mutableMapOf<String, FreightInvoiceSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceScheduleStore(
    var inner: FreightInvoiceScheduleStore,
    var cache: TtlCache<FreightInvoiceSchedule>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceScheduleStore {
    override fun find(code: String): FreightInvoiceSchedule? {
        val hit = (cache.get(code) as FreightInvoiceSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceSchedule> = inner.list()
}

fun newCachedFreightInvoiceScheduleStore(inner: FreightInvoiceScheduleStore, ttl: Int, clock: Clock): FreightInvoiceScheduleStore {
    return CachedFreightInvoiceScheduleStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice schedules. Empty fields match all. */
data class FreightInvoiceScheduleQuery(
    var locale: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceSchedule>): MutableList<FreightInvoiceSchedule> {
        val matched = filter(items) { candidate: FreightInvoiceSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceSchedules(store: FreightInvoiceScheduleStore, query: FreightInvoiceScheduleQuery): MutableList<FreightInvoiceSchedule> {
    return query.apply(store.list())
}
