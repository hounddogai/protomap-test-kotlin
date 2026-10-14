package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan reservations by code. */
interface LanePlanReservationStore {
    fun find(code: String): LanePlanReservation?
    fun save(item: LanePlanReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanReservationStore(
    var items: MutableMap<String, LanePlanReservation>,
    var order: MutableList<String>,
) : LanePlanReservationStore {
    override fun find(code: String): LanePlanReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanReservation) {
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

    override fun list(): MutableList<LanePlanReservation> {
        val out = mutableListOf<LanePlanReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanReservationStore(): LanePlanReservationStore {
    return MemoryLanePlanReservationStore(mutableMapOf<String, LanePlanReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanReservationStore(
    var inner: LanePlanReservationStore,
    var cache: TtlCache<LanePlanReservation>,
    var hits: Int,
    var misses: Int,
) : LanePlanReservationStore {
    override fun find(code: String): LanePlanReservation? {
        val hit = (cache.get(code) as LanePlanReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanReservation> = inner.list()
}

fun newCachedLanePlanReservationStore(inner: LanePlanReservationStore, ttl: Int, clock: Clock): LanePlanReservationStore {
    return CachedLanePlanReservationStore(
        inner = inner,
        cache = newTtlCache<LanePlanReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan reservations. Empty fields match all. */
data class LanePlanReservationQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanReservation>): MutableList<LanePlanReservation> {
        val matched = filter(items) { candidate: LanePlanReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanReservations(store: LanePlanReservationStore, query: LanePlanReservationQuery): MutableList<LanePlanReservation> {
    return query.apply(store.list())
}
