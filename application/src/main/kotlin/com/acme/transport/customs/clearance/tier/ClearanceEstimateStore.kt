package com.acme.transport.customs.clearance.tier

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance estimates by code. */
interface ClearanceEstimateStore {
    fun find(code: String): ClearanceEstimate?
    fun save(item: ClearanceEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceEstimateStore(
    var items: MutableMap<String, ClearanceEstimate>,
    var order: MutableList<String>,
) : ClearanceEstimateStore {
    override fun find(code: String): ClearanceEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceEstimate) {
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

    override fun list(): MutableList<ClearanceEstimate> {
        val out = mutableListOf<ClearanceEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceEstimateStore(): ClearanceEstimateStore {
    return MemoryClearanceEstimateStore(mutableMapOf<String, ClearanceEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceEstimateStore(
    var inner: ClearanceEstimateStore,
    var cache: TtlCache<ClearanceEstimate>,
    var hits: Int,
    var misses: Int,
) : ClearanceEstimateStore {
    override fun find(code: String): ClearanceEstimate? {
        val hit = (cache.get(code) as ClearanceEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceEstimate> = inner.list()
}

fun newCachedClearanceEstimateStore(inner: ClearanceEstimateStore, ttl: Int, clock: Clock): ClearanceEstimateStore {
    return CachedClearanceEstimateStore(
        inner = inner,
        cache = newTtlCache<ClearanceEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance estimates. Empty fields match all. */
data class ClearanceEstimateQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceEstimate>): MutableList<ClearanceEstimate> {
        val matched = filter(items) { candidate: ClearanceEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceEstimates(store: ClearanceEstimateStore, query: ClearanceEstimateQuery): MutableList<ClearanceEstimate> {
    return query.apply(store.list())
}
