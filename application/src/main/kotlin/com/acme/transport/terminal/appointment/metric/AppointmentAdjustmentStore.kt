package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment adjustments by code. */
interface AppointmentAdjustmentStore {
    fun find(code: String): AppointmentAdjustment?
    fun save(item: AppointmentAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentAdjustmentStore(
    var items: MutableMap<String, AppointmentAdjustment>,
    var order: MutableList<String>,
) : AppointmentAdjustmentStore {
    override fun find(code: String): AppointmentAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentAdjustment) {
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

    override fun list(): MutableList<AppointmentAdjustment> {
        val out = mutableListOf<AppointmentAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentAdjustmentStore(): AppointmentAdjustmentStore {
    return MemoryAppointmentAdjustmentStore(
        mutableMapOf<String, AppointmentAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentAdjustmentStore(
    var inner: AppointmentAdjustmentStore,
    var cache: TtlCache<AppointmentAdjustment>,
    var hits: Int,
    var misses: Int,
) : AppointmentAdjustmentStore {
    override fun find(code: String): AppointmentAdjustment? {
        val hit = (cache.get(code) as AppointmentAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentAdjustment> = inner.list()
}

fun newCachedAppointmentAdjustmentStore(inner: AppointmentAdjustmentStore, ttl: Int, clock: Clock): AppointmentAdjustmentStore {
    return CachedAppointmentAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AppointmentAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment adjustments. Empty fields match all. */
data class AppointmentAdjustmentQuery(
    var source: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentAdjustment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentAdjustment>): MutableList<AppointmentAdjustment> {
        val matched = filter(items) { candidate: AppointmentAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentAdjustments(store: AppointmentAdjustmentStore, query: AppointmentAdjustmentQuery): MutableList<AppointmentAdjustment> {
    return query.apply(store.list())
}
