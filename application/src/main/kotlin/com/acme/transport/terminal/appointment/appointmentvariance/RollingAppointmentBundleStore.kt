package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling appointment bundles by code. */
interface RollingAppointmentBundleStore {
    fun find(code: String): RollingAppointmentBundle?
    fun save(item: RollingAppointmentBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAppointmentBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAppointmentBundleStore(
    var items: MutableMap<String, RollingAppointmentBundle>,
    var order: MutableList<String>,
) : RollingAppointmentBundleStore {
    override fun find(code: String): RollingAppointmentBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAppointmentBundle) {
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

    override fun list(): MutableList<RollingAppointmentBundle> {
        val out = mutableListOf<RollingAppointmentBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAppointmentBundleStore(): RollingAppointmentBundleStore {
    return MemoryRollingAppointmentBundleStore(
        mutableMapOf<String, RollingAppointmentBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAppointmentBundleStore(
    var inner: RollingAppointmentBundleStore,
    var cache: TtlCache<RollingAppointmentBundle>,
    var hits: Int,
    var misses: Int,
) : RollingAppointmentBundleStore {
    override fun find(code: String): RollingAppointmentBundle? {
        val hit = (cache.get(code) as RollingAppointmentBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAppointmentBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAppointmentBundle> = inner.list()
}

fun newCachedRollingAppointmentBundleStore(inner: RollingAppointmentBundleStore, ttl: Int, clock: Clock): RollingAppointmentBundleStore {
    return CachedRollingAppointmentBundleStore(
        inner = inner,
        cache = newTtlCache<RollingAppointmentBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling appointment bundles. Empty fields match all. */
data class RollingAppointmentBundleQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAppointmentBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAppointmentBundle>): MutableList<RollingAppointmentBundle> {
        val matched = filter(items) { candidate: RollingAppointmentBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAppointmentBundles(store: RollingAppointmentBundleStore, query: RollingAppointmentBundleQuery): MutableList<RollingAppointmentBundle> {
    return query.apply(store.list())
}
