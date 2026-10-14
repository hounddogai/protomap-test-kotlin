package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted appointment scores by code. */
interface WeightedAppointmentScoreStore {
    fun find(code: String): WeightedAppointmentScore?
    fun save(item: WeightedAppointmentScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAppointmentScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAppointmentScoreStore(
    var items: MutableMap<String, WeightedAppointmentScore>,
    var order: MutableList<String>,
) : WeightedAppointmentScoreStore {
    override fun find(code: String): WeightedAppointmentScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAppointmentScore) {
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

    override fun list(): MutableList<WeightedAppointmentScore> {
        val out = mutableListOf<WeightedAppointmentScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAppointmentScoreStore(): WeightedAppointmentScoreStore {
    return MemoryWeightedAppointmentScoreStore(
        mutableMapOf<String, WeightedAppointmentScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAppointmentScoreStore(
    var inner: WeightedAppointmentScoreStore,
    var cache: TtlCache<WeightedAppointmentScore>,
    var hits: Int,
    var misses: Int,
) : WeightedAppointmentScoreStore {
    override fun find(code: String): WeightedAppointmentScore? {
        val hit = (cache.get(code) as WeightedAppointmentScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAppointmentScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAppointmentScore> = inner.list()
}

fun newCachedWeightedAppointmentScoreStore(inner: WeightedAppointmentScoreStore, ttl: Int, clock: Clock): WeightedAppointmentScoreStore {
    return CachedWeightedAppointmentScoreStore(
        inner = inner,
        cache = newTtlCache<WeightedAppointmentScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted appointment scores. Empty fields match all. */
data class WeightedAppointmentScoreQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAppointmentScore): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAppointmentScore>): MutableList<WeightedAppointmentScore> {
        val matched = filter(items) { candidate: WeightedAppointmentScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAppointmentScores(store: WeightedAppointmentScoreStore, query: WeightedAppointmentScoreQuery): MutableList<WeightedAppointmentScore> {
    return query.apply(store.list())
}
