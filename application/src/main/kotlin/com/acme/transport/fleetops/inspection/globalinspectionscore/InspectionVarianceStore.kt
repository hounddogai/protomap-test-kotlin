package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection variances by code. */
interface InspectionVarianceStore {
    fun find(code: String): InspectionVariance?
    fun save(item: InspectionVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionVarianceStore(
    var items: MutableMap<String, InspectionVariance>,
    var order: MutableList<String>,
) : InspectionVarianceStore {
    override fun find(code: String): InspectionVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionVariance) {
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

    override fun list(): MutableList<InspectionVariance> {
        val out = mutableListOf<InspectionVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionVarianceStore(): InspectionVarianceStore {
    return MemoryInspectionVarianceStore(mutableMapOf<String, InspectionVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionVarianceStore(
    var inner: InspectionVarianceStore,
    var cache: TtlCache<InspectionVariance>,
    var hits: Int,
    var misses: Int,
) : InspectionVarianceStore {
    override fun find(code: String): InspectionVariance? {
        val hit = (cache.get(code) as InspectionVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionVariance> = inner.list()
}

fun newCachedInspectionVarianceStore(inner: InspectionVarianceStore, ttl: Int, clock: Clock): InspectionVarianceStore {
    return CachedInspectionVarianceStore(
        inner = inner,
        cache = newTtlCache<InspectionVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection variances. Empty fields match all. */
data class InspectionVarianceQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionVariance>): MutableList<InspectionVariance> {
        val matched = filter(items) { candidate: InspectionVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionVariances(store: InspectionVarianceStore, query: InspectionVarianceQuery): MutableList<InspectionVariance> {
    return query.apply(store.list())
}
