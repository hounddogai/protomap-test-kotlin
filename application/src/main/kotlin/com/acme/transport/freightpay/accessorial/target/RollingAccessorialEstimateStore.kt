package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling accessorial estimates by code. */
interface RollingAccessorialEstimateStore {
    fun find(code: String): RollingAccessorialEstimate?
    fun save(item: RollingAccessorialEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAccessorialEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAccessorialEstimateStore(
    var items: MutableMap<String, RollingAccessorialEstimate>,
    var order: MutableList<String>,
) : RollingAccessorialEstimateStore {
    override fun find(code: String): RollingAccessorialEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAccessorialEstimate) {
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

    override fun list(): MutableList<RollingAccessorialEstimate> {
        val out = mutableListOf<RollingAccessorialEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAccessorialEstimateStore(): RollingAccessorialEstimateStore {
    return MemoryRollingAccessorialEstimateStore(
        mutableMapOf<String, RollingAccessorialEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAccessorialEstimateStore(
    var inner: RollingAccessorialEstimateStore,
    var cache: TtlCache<RollingAccessorialEstimate>,
    var hits: Int,
    var misses: Int,
) : RollingAccessorialEstimateStore {
    override fun find(code: String): RollingAccessorialEstimate? {
        val hit = (cache.get(code) as RollingAccessorialEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAccessorialEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAccessorialEstimate> = inner.list()
}

fun newCachedRollingAccessorialEstimateStore(inner: RollingAccessorialEstimateStore, ttl: Int, clock: Clock): RollingAccessorialEstimateStore {
    return CachedRollingAccessorialEstimateStore(
        inner = inner,
        cache = newTtlCache<RollingAccessorialEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling accessorial estimates. Empty fields match all. */
data class RollingAccessorialEstimateQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAccessorialEstimate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAccessorialEstimate>): MutableList<RollingAccessorialEstimate> {
        val matched = filter(items) { candidate: RollingAccessorialEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAccessorialEstimates(store: RollingAccessorialEstimateStore, query: RollingAccessorialEstimateQuery): MutableList<RollingAccessorialEstimate> {
    return query.apply(store.list())
}
