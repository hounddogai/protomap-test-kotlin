package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection exceptions by code. */
interface WeightedInspectionExceptionStore {
    fun find(code: String): WeightedInspectionException?
    fun save(item: WeightedInspectionException)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionExceptionStore(
    var items: MutableMap<String, WeightedInspectionException>,
    var order: MutableList<String>,
) : WeightedInspectionExceptionStore {
    override fun find(code: String): WeightedInspectionException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionException) {
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

    override fun list(): MutableList<WeightedInspectionException> {
        val out = mutableListOf<WeightedInspectionException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionExceptionStore(): WeightedInspectionExceptionStore {
    return MemoryWeightedInspectionExceptionStore(
        mutableMapOf<String, WeightedInspectionException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionExceptionStore(
    var inner: WeightedInspectionExceptionStore,
    var cache: TtlCache<WeightedInspectionException>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionExceptionStore {
    override fun find(code: String): WeightedInspectionException? {
        val hit = (cache.get(code) as WeightedInspectionException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionException> = inner.list()
}

fun newCachedWeightedInspectionExceptionStore(inner: WeightedInspectionExceptionStore, ttl: Int, clock: Clock): WeightedInspectionExceptionStore {
    return CachedWeightedInspectionExceptionStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection exceptions. Empty fields match all. */
data class WeightedInspectionExceptionQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedInspectionException>): MutableList<WeightedInspectionException> {
        val matched = filter(items) { candidate: WeightedInspectionException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionExceptions(store: WeightedInspectionExceptionStore, query: WeightedInspectionExceptionQuery): MutableList<WeightedInspectionException> {
    return query.apply(store.list())
}
