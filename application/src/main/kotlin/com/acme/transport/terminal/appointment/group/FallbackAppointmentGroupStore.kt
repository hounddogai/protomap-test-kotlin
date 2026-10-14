package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback appointment groups by code. */
interface FallbackAppointmentGroupStore {
    fun find(code: String): FallbackAppointmentGroup?
    fun save(item: FallbackAppointmentGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAppointmentGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAppointmentGroupStore(
    var items: MutableMap<String, FallbackAppointmentGroup>,
    var order: MutableList<String>,
) : FallbackAppointmentGroupStore {
    override fun find(code: String): FallbackAppointmentGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAppointmentGroup) {
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

    override fun list(): MutableList<FallbackAppointmentGroup> {
        val out = mutableListOf<FallbackAppointmentGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAppointmentGroupStore(): FallbackAppointmentGroupStore {
    return MemoryFallbackAppointmentGroupStore(
        mutableMapOf<String, FallbackAppointmentGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAppointmentGroupStore(
    var inner: FallbackAppointmentGroupStore,
    var cache: TtlCache<FallbackAppointmentGroup>,
    var hits: Int,
    var misses: Int,
) : FallbackAppointmentGroupStore {
    override fun find(code: String): FallbackAppointmentGroup? {
        val hit = (cache.get(code) as FallbackAppointmentGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAppointmentGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAppointmentGroup> = inner.list()
}

fun newCachedFallbackAppointmentGroupStore(inner: FallbackAppointmentGroupStore, ttl: Int, clock: Clock): FallbackAppointmentGroupStore {
    return CachedFallbackAppointmentGroupStore(
        inner = inner,
        cache = newTtlCache<FallbackAppointmentGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback appointment groups. Empty fields match all. */
data class FallbackAppointmentGroupQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAppointmentGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAppointmentGroup>): MutableList<FallbackAppointmentGroup> {
        val matched = filter(items) { candidate: FallbackAppointmentGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAppointmentGroups(store: FallbackAppointmentGroupStore, query: FallbackAppointmentGroupQuery): MutableList<FallbackAppointmentGroup> {
    return query.apply(store.list())
}
