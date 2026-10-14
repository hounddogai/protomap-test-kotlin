package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default inspection estimates by code. */
interface DefaultInspectionEstimateStore {
    fun find(code: String): DefaultInspectionEstimate?
    fun save(item: DefaultInspectionEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultInspectionEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultInspectionEstimateStore(
    var items: MutableMap<String, DefaultInspectionEstimate>,
    var order: MutableList<String>,
) : DefaultInspectionEstimateStore {
    override fun find(code: String): DefaultInspectionEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultInspectionEstimate) {
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

    override fun list(): MutableList<DefaultInspectionEstimate> {
        val out = mutableListOf<DefaultInspectionEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultInspectionEstimateStore(): DefaultInspectionEstimateStore {
    return MemoryDefaultInspectionEstimateStore(
        mutableMapOf<String, DefaultInspectionEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultInspectionEstimateStore(
    var inner: DefaultInspectionEstimateStore,
    var cache: TtlCache<DefaultInspectionEstimate>,
    var hits: Int,
    var misses: Int,
) : DefaultInspectionEstimateStore {
    override fun find(code: String): DefaultInspectionEstimate? {
        val hit = (cache.get(code) as DefaultInspectionEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultInspectionEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultInspectionEstimate> = inner.list()
}

fun newCachedDefaultInspectionEstimateStore(inner: DefaultInspectionEstimateStore, ttl: Int, clock: Clock): DefaultInspectionEstimateStore {
    return CachedDefaultInspectionEstimateStore(
        inner = inner,
        cache = newTtlCache<DefaultInspectionEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default inspection estimates. Empty fields match all. */
data class DefaultInspectionEstimateQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultInspectionEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultInspectionEstimate>): MutableList<DefaultInspectionEstimate> {
        val matched = filter(items) { candidate: DefaultInspectionEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultInspectionEstimates(store: DefaultInspectionEstimateStore, query: DefaultInspectionEstimateQuery): MutableList<DefaultInspectionEstimate> {
    return query.apply(store.list())
}
