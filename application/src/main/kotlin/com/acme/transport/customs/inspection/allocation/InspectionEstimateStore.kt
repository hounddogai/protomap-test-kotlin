package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection estimates by code. */
interface InspectionEstimateStore {
    fun find(code: String): InspectionEstimate?
    fun save(item: InspectionEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionEstimateStore(
    var items: MutableMap<String, InspectionEstimate>,
    var order: MutableList<String>,
) : InspectionEstimateStore {
    override fun find(code: String): InspectionEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionEstimate) {
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

    override fun list(): MutableList<InspectionEstimate> {
        val out = mutableListOf<InspectionEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionEstimateStore(): InspectionEstimateStore {
    return MemoryInspectionEstimateStore(mutableMapOf<String, InspectionEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionEstimateStore(
    var inner: InspectionEstimateStore,
    var cache: TtlCache<InspectionEstimate>,
    var hits: Int,
    var misses: Int,
) : InspectionEstimateStore {
    override fun find(code: String): InspectionEstimate? {
        val hit = (cache.get(code) as InspectionEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionEstimate> = inner.list()
}

fun newCachedInspectionEstimateStore(inner: InspectionEstimateStore, ttl: Int, clock: Clock): InspectionEstimateStore {
    return CachedInspectionEstimateStore(
        inner = inner,
        cache = newTtlCache<InspectionEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection estimates. Empty fields match all. */
data class InspectionEstimateQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionEstimate>): MutableList<InspectionEstimate> {
        val matched = filter(items) { candidate: InspectionEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionEstimates(store: InspectionEstimateStore, query: InspectionEstimateQuery): MutableList<InspectionEstimate> {
    return query.apply(store.list())
}
