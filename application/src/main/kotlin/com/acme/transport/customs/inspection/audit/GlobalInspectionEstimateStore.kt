package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection estimates by code. */
interface GlobalInspectionEstimateStore {
    fun find(code: String): GlobalInspectionEstimate?
    fun save(item: GlobalInspectionEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionEstimateStore(
    var items: MutableMap<String, GlobalInspectionEstimate>,
    var order: MutableList<String>,
) : GlobalInspectionEstimateStore {
    override fun find(code: String): GlobalInspectionEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionEstimate) {
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

    override fun list(): MutableList<GlobalInspectionEstimate> {
        val out = mutableListOf<GlobalInspectionEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionEstimateStore(): GlobalInspectionEstimateStore {
    return MemoryGlobalInspectionEstimateStore(
        mutableMapOf<String, GlobalInspectionEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionEstimateStore(
    var inner: GlobalInspectionEstimateStore,
    var cache: TtlCache<GlobalInspectionEstimate>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionEstimateStore {
    override fun find(code: String): GlobalInspectionEstimate? {
        val hit = (cache.get(code) as GlobalInspectionEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionEstimate> = inner.list()
}

fun newCachedGlobalInspectionEstimateStore(inner: GlobalInspectionEstimateStore, ttl: Int, clock: Clock): GlobalInspectionEstimateStore {
    return CachedGlobalInspectionEstimateStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection estimates. Empty fields match all. */
data class GlobalInspectionEstimateQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalInspectionEstimate>): MutableList<GlobalInspectionEstimate> {
        val matched = filter(items) { candidate: GlobalInspectionEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionEstimates(store: GlobalInspectionEstimateStore, query: GlobalInspectionEstimateQuery): MutableList<GlobalInspectionEstimate> {
    return query.apply(store.list())
}
