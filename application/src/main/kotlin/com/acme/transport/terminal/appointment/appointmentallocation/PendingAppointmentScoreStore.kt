package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending appointment scores by code. */
interface PendingAppointmentScoreStore {
    fun find(code: String): PendingAppointmentScore?
    fun save(item: PendingAppointmentScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAppointmentScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAppointmentScoreStore(
    var items: MutableMap<String, PendingAppointmentScore>,
    var order: MutableList<String>,
) : PendingAppointmentScoreStore {
    override fun find(code: String): PendingAppointmentScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAppointmentScore) {
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

    override fun list(): MutableList<PendingAppointmentScore> {
        val out = mutableListOf<PendingAppointmentScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAppointmentScoreStore(): PendingAppointmentScoreStore {
    return MemoryPendingAppointmentScoreStore(
        mutableMapOf<String, PendingAppointmentScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAppointmentScoreStore(
    var inner: PendingAppointmentScoreStore,
    var cache: TtlCache<PendingAppointmentScore>,
    var hits: Int,
    var misses: Int,
) : PendingAppointmentScoreStore {
    override fun find(code: String): PendingAppointmentScore? {
        val hit = (cache.get(code) as PendingAppointmentScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAppointmentScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAppointmentScore> = inner.list()
}

fun newCachedPendingAppointmentScoreStore(inner: PendingAppointmentScoreStore, ttl: Int, clock: Clock): PendingAppointmentScoreStore {
    return CachedPendingAppointmentScoreStore(
        inner = inner,
        cache = newTtlCache<PendingAppointmentScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending appointment scores. Empty fields match all. */
data class PendingAppointmentScoreQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAppointmentScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAppointmentScore>): MutableList<PendingAppointmentScore> {
        val matched = filter(items) { candidate: PendingAppointmentScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAppointmentScores(store: PendingAppointmentScoreStore, query: PendingAppointmentScoreQuery): MutableList<PendingAppointmentScore> {
    return query.apply(store.list())
}
