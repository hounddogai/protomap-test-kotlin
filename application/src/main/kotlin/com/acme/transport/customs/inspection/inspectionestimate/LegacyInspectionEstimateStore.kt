package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection estimates by code. */
interface LegacyInspectionEstimateStore {
    fun find(code: String): LegacyInspectionEstimate?
    fun save(item: LegacyInspectionEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionEstimateStore(
    var items: MutableMap<String, LegacyInspectionEstimate>,
    var order: MutableList<String>,
) : LegacyInspectionEstimateStore {
    override fun find(code: String): LegacyInspectionEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionEstimate) {
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

    override fun list(): MutableList<LegacyInspectionEstimate> {
        val out = mutableListOf<LegacyInspectionEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionEstimateStore(): LegacyInspectionEstimateStore {
    return MemoryLegacyInspectionEstimateStore(
        mutableMapOf<String, LegacyInspectionEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionEstimateStore(
    var inner: LegacyInspectionEstimateStore,
    var cache: TtlCache<LegacyInspectionEstimate>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionEstimateStore {
    override fun find(code: String): LegacyInspectionEstimate? {
        val hit = (cache.get(code) as LegacyInspectionEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionEstimate> = inner.list()
}

fun newCachedLegacyInspectionEstimateStore(inner: LegacyInspectionEstimateStore, ttl: Int, clock: Clock): LegacyInspectionEstimateStore {
    return CachedLegacyInspectionEstimateStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection estimates. Empty fields match all. */
data class LegacyInspectionEstimateQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionEstimate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyInspectionEstimate>): MutableList<LegacyInspectionEstimate> {
        val matched = filter(items) { candidate: LegacyInspectionEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionEstimates(store: LegacyInspectionEstimateStore, query: LegacyInspectionEstimateQuery): MutableList<LegacyInspectionEstimate> {
    return query.apply(store.list())
}
