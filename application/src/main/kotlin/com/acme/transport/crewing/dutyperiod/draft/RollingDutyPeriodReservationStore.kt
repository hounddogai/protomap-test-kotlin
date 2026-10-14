package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling duty period reservations by code. */
interface RollingDutyPeriodReservationStore {
    fun find(code: String): RollingDutyPeriodReservation?
    fun save(item: RollingDutyPeriodReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDutyPeriodReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDutyPeriodReservationStore(
    var items: MutableMap<String, RollingDutyPeriodReservation>,
    var order: MutableList<String>,
) : RollingDutyPeriodReservationStore {
    override fun find(code: String): RollingDutyPeriodReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDutyPeriodReservation) {
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

    override fun list(): MutableList<RollingDutyPeriodReservation> {
        val out = mutableListOf<RollingDutyPeriodReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDutyPeriodReservationStore(): RollingDutyPeriodReservationStore {
    return MemoryRollingDutyPeriodReservationStore(
        mutableMapOf<String, RollingDutyPeriodReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDutyPeriodReservationStore(
    var inner: RollingDutyPeriodReservationStore,
    var cache: TtlCache<RollingDutyPeriodReservation>,
    var hits: Int,
    var misses: Int,
) : RollingDutyPeriodReservationStore {
    override fun find(code: String): RollingDutyPeriodReservation? {
        val hit = (cache.get(code) as RollingDutyPeriodReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDutyPeriodReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDutyPeriodReservation> = inner.list()
}

fun newCachedRollingDutyPeriodReservationStore(inner: RollingDutyPeriodReservationStore, ttl: Int, clock: Clock): RollingDutyPeriodReservationStore {
    return CachedRollingDutyPeriodReservationStore(
        inner = inner,
        cache = newTtlCache<RollingDutyPeriodReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling duty period reservations. Empty fields match all. */
data class RollingDutyPeriodReservationQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDutyPeriodReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDutyPeriodReservation>): MutableList<RollingDutyPeriodReservation> {
        val matched = filter(items) { candidate: RollingDutyPeriodReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDutyPeriodReservations(store: RollingDutyPeriodReservationStore, query: RollingDutyPeriodReservationQuery): MutableList<RollingDutyPeriodReservation> {
    return query.apply(store.list())
}
