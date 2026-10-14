package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted appointment mappings by code. */
interface WeightedAppointmentMappingStore {
    fun find(code: String): WeightedAppointmentMapping?
    fun save(item: WeightedAppointmentMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAppointmentMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAppointmentMappingStore(
    var items: MutableMap<String, WeightedAppointmentMapping>,
    var order: MutableList<String>,
) : WeightedAppointmentMappingStore {
    override fun find(code: String): WeightedAppointmentMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAppointmentMapping) {
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

    override fun list(): MutableList<WeightedAppointmentMapping> {
        val out = mutableListOf<WeightedAppointmentMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAppointmentMappingStore(): WeightedAppointmentMappingStore {
    return MemoryWeightedAppointmentMappingStore(
        mutableMapOf<String, WeightedAppointmentMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAppointmentMappingStore(
    var inner: WeightedAppointmentMappingStore,
    var cache: TtlCache<WeightedAppointmentMapping>,
    var hits: Int,
    var misses: Int,
) : WeightedAppointmentMappingStore {
    override fun find(code: String): WeightedAppointmentMapping? {
        val hit = (cache.get(code) as WeightedAppointmentMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAppointmentMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAppointmentMapping> = inner.list()
}

fun newCachedWeightedAppointmentMappingStore(inner: WeightedAppointmentMappingStore, ttl: Int, clock: Clock): WeightedAppointmentMappingStore {
    return CachedWeightedAppointmentMappingStore(
        inner = inner,
        cache = newTtlCache<WeightedAppointmentMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted appointment mappings. Empty fields match all. */
data class WeightedAppointmentMappingQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAppointmentMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAppointmentMapping>): MutableList<WeightedAppointmentMapping> {
        val matched = filter(items) { candidate: WeightedAppointmentMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAppointmentMappings(store: WeightedAppointmentMappingStore, query: WeightedAppointmentMappingQuery): MutableList<WeightedAppointmentMapping> {
    return query.apply(store.list())
}
