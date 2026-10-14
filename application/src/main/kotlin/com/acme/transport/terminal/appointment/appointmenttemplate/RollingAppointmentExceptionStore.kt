package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling appointment exceptions by code. */
interface RollingAppointmentExceptionStore {
    fun find(code: String): RollingAppointmentException?
    fun save(item: RollingAppointmentException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAppointmentException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAppointmentExceptionStore(
    var items: MutableMap<String, RollingAppointmentException>,
    var order: MutableList<String>,
) : RollingAppointmentExceptionStore {
    override fun find(code: String): RollingAppointmentException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAppointmentException) {
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

    override fun list(): MutableList<RollingAppointmentException> {
        val out = mutableListOf<RollingAppointmentException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAppointmentExceptionStore(): RollingAppointmentExceptionStore {
    return MemoryRollingAppointmentExceptionStore(
        mutableMapOf<String, RollingAppointmentException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAppointmentExceptionStore(
    var inner: RollingAppointmentExceptionStore,
    var cache: TtlCache<RollingAppointmentException>,
    var hits: Int,
    var misses: Int,
) : RollingAppointmentExceptionStore {
    override fun find(code: String): RollingAppointmentException? {
        val hit = (cache.get(code) as RollingAppointmentException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAppointmentException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAppointmentException> = inner.list()
}

fun newCachedRollingAppointmentExceptionStore(inner: RollingAppointmentExceptionStore, ttl: Int, clock: Clock): RollingAppointmentExceptionStore {
    return CachedRollingAppointmentExceptionStore(
        inner = inner,
        cache = newTtlCache<RollingAppointmentException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling appointment exceptions. Empty fields match all. */
data class RollingAppointmentExceptionQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAppointmentException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAppointmentException>): MutableList<RollingAppointmentException> {
        val matched = filter(items) { candidate: RollingAppointmentException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAppointmentExceptions(store: RollingAppointmentExceptionStore, query: RollingAppointmentExceptionQuery): MutableList<RollingAppointmentException> {
    return query.apply(store.list())
}
