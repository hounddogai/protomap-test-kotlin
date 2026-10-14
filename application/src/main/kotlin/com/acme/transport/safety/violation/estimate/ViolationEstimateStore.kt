package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation estimates by code. */
interface ViolationEstimateStore {
    fun find(code: String): ViolationEstimate?
    fun save(item: ViolationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationEstimateStore(
    var items: MutableMap<String, ViolationEstimate>,
    var order: MutableList<String>,
) : ViolationEstimateStore {
    override fun find(code: String): ViolationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationEstimate) {
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

    override fun list(): MutableList<ViolationEstimate> {
        val out = mutableListOf<ViolationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationEstimateStore(): ViolationEstimateStore {
    return MemoryViolationEstimateStore(mutableMapOf<String, ViolationEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationEstimateStore(
    var inner: ViolationEstimateStore,
    var cache: TtlCache<ViolationEstimate>,
    var hits: Int,
    var misses: Int,
) : ViolationEstimateStore {
    override fun find(code: String): ViolationEstimate? {
        val hit = (cache.get(code) as ViolationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationEstimate> = inner.list()
}

fun newCachedViolationEstimateStore(inner: ViolationEstimateStore, ttl: Int, clock: Clock): ViolationEstimateStore {
    return CachedViolationEstimateStore(
        inner = inner,
        cache = newTtlCache<ViolationEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation estimates. Empty fields match all. */
data class ViolationEstimateQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ViolationEstimate>): MutableList<ViolationEstimate> {
        val matched = filter(items) { candidate: ViolationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationEstimates(store: ViolationEstimateStore, query: ViolationEstimateQuery): MutableList<ViolationEstimate> {
    return query.apply(store.list())
}
