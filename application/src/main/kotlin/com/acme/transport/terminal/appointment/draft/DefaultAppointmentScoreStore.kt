package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default appointment scores by code. */
interface DefaultAppointmentScoreStore {
    fun find(code: String): DefaultAppointmentScore?
    fun save(item: DefaultAppointmentScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAppointmentScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAppointmentScoreStore(
    var items: MutableMap<String, DefaultAppointmentScore>,
    var order: MutableList<String>,
) : DefaultAppointmentScoreStore {
    override fun find(code: String): DefaultAppointmentScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAppointmentScore) {
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

    override fun list(): MutableList<DefaultAppointmentScore> {
        val out = mutableListOf<DefaultAppointmentScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAppointmentScoreStore(): DefaultAppointmentScoreStore {
    return MemoryDefaultAppointmentScoreStore(
        mutableMapOf<String, DefaultAppointmentScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAppointmentScoreStore(
    var inner: DefaultAppointmentScoreStore,
    var cache: TtlCache<DefaultAppointmentScore>,
    var hits: Int,
    var misses: Int,
) : DefaultAppointmentScoreStore {
    override fun find(code: String): DefaultAppointmentScore? {
        val hit = (cache.get(code) as DefaultAppointmentScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAppointmentScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAppointmentScore> = inner.list()
}

fun newCachedDefaultAppointmentScoreStore(inner: DefaultAppointmentScoreStore, ttl: Int, clock: Clock): DefaultAppointmentScoreStore {
    return CachedDefaultAppointmentScoreStore(
        inner = inner,
        cache = newTtlCache<DefaultAppointmentScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default appointment scores. Empty fields match all. */
data class DefaultAppointmentScoreQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAppointmentScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAppointmentScore>): MutableList<DefaultAppointmentScore> {
        val matched = filter(items) { candidate: DefaultAppointmentScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAppointmentScores(store: DefaultAppointmentScoreStore, query: DefaultAppointmentScoreQuery): MutableList<DefaultAppointmentScore> {
    return query.apply(store.list())
}
