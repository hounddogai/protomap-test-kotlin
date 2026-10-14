package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect estimates by code. */
interface DefectEstimateStore {
    fun find(code: String): DefectEstimate?
    fun save(item: DefectEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectEstimateStore(
    var items: MutableMap<String, DefectEstimate>,
    var order: MutableList<String>,
) : DefectEstimateStore {
    override fun find(code: String): DefectEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectEstimate) {
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

    override fun list(): MutableList<DefectEstimate> {
        val out = mutableListOf<DefectEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectEstimateStore(): DefectEstimateStore {
    return MemoryDefectEstimateStore(mutableMapOf<String, DefectEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectEstimateStore(
    var inner: DefectEstimateStore,
    var cache: TtlCache<DefectEstimate>,
    var hits: Int,
    var misses: Int,
) : DefectEstimateStore {
    override fun find(code: String): DefectEstimate? {
        val hit = (cache.get(code) as DefectEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectEstimate> = inner.list()
}

fun newCachedDefectEstimateStore(inner: DefectEstimateStore, ttl: Int, clock: Clock): DefectEstimateStore {
    return CachedDefectEstimateStore(
        inner = inner,
        cache = newTtlCache<DefectEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect estimates. Empty fields match all. */
data class DefectEstimateQuery(
    var country: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectEstimate>): MutableList<DefectEstimate> {
        val matched = filter(items) { candidate: DefectEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectEstimates(store: DefectEstimateStore, query: DefectEstimateQuery): MutableList<DefectEstimate> {
    return query.apply(store.list())
}
