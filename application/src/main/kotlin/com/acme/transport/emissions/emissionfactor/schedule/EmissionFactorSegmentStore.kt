package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor segments by code. */
interface EmissionFactorSegmentStore {
    fun find(code: String): EmissionFactorSegment?
    fun save(item: EmissionFactorSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorSegmentStore(
    var items: MutableMap<String, EmissionFactorSegment>,
    var order: MutableList<String>,
) : EmissionFactorSegmentStore {
    override fun find(code: String): EmissionFactorSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorSegment) {
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

    override fun list(): MutableList<EmissionFactorSegment> {
        val out = mutableListOf<EmissionFactorSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorSegmentStore(): EmissionFactorSegmentStore {
    return MemoryEmissionFactorSegmentStore(
        mutableMapOf<String, EmissionFactorSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorSegmentStore(
    var inner: EmissionFactorSegmentStore,
    var cache: TtlCache<EmissionFactorSegment>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorSegmentStore {
    override fun find(code: String): EmissionFactorSegment? {
        val hit = (cache.get(code) as EmissionFactorSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorSegment> = inner.list()
}

fun newCachedEmissionFactorSegmentStore(inner: EmissionFactorSegmentStore, ttl: Int, clock: Clock): EmissionFactorSegmentStore {
    return CachedEmissionFactorSegmentStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor segments. Empty fields match all. */
data class EmissionFactorSegmentQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorSegment>): MutableList<EmissionFactorSegment> {
        val matched = filter(items) { candidate: EmissionFactorSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorSegments(store: EmissionFactorSegmentStore, query: EmissionFactorSegmentQuery): MutableList<EmissionFactorSegment> {
    return query.apply(store.list())
}
