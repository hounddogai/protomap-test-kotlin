package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending appointment baselines by code. */
interface PendingAppointmentBaselineStore {
    fun find(code: String): PendingAppointmentBaseline?
    fun save(item: PendingAppointmentBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAppointmentBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAppointmentBaselineStore(
    var items: MutableMap<String, PendingAppointmentBaseline>,
    var order: MutableList<String>,
) : PendingAppointmentBaselineStore {
    override fun find(code: String): PendingAppointmentBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAppointmentBaseline) {
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

    override fun list(): MutableList<PendingAppointmentBaseline> {
        val out = mutableListOf<PendingAppointmentBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAppointmentBaselineStore(): PendingAppointmentBaselineStore {
    return MemoryPendingAppointmentBaselineStore(
        mutableMapOf<String, PendingAppointmentBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAppointmentBaselineStore(
    var inner: PendingAppointmentBaselineStore,
    var cache: TtlCache<PendingAppointmentBaseline>,
    var hits: Int,
    var misses: Int,
) : PendingAppointmentBaselineStore {
    override fun find(code: String): PendingAppointmentBaseline? {
        val hit = (cache.get(code) as PendingAppointmentBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAppointmentBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAppointmentBaseline> = inner.list()
}

fun newCachedPendingAppointmentBaselineStore(inner: PendingAppointmentBaselineStore, ttl: Int, clock: Clock): PendingAppointmentBaselineStore {
    return CachedPendingAppointmentBaselineStore(
        inner = inner,
        cache = newTtlCache<PendingAppointmentBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending appointment baselines. Empty fields match all. */
data class PendingAppointmentBaselineQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAppointmentBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAppointmentBaseline>): MutableList<PendingAppointmentBaseline> {
        val matched = filter(items) { candidate: PendingAppointmentBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAppointmentBaselines(store: PendingAppointmentBaselineStore, query: PendingAppointmentBaselineQuery): MutableList<PendingAppointmentBaseline> {
    return query.apply(store.list())
}
