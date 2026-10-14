package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual utilization variances by code. */
interface ManualUtilizationVarianceStore {
    fun find(code: String): ManualUtilizationVariance?
    fun save(item: ManualUtilizationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualUtilizationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualUtilizationVarianceStore(
    var items: MutableMap<String, ManualUtilizationVariance>,
    var order: MutableList<String>,
) : ManualUtilizationVarianceStore {
    override fun find(code: String): ManualUtilizationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualUtilizationVariance) {
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

    override fun list(): MutableList<ManualUtilizationVariance> {
        val out = mutableListOf<ManualUtilizationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualUtilizationVarianceStore(): ManualUtilizationVarianceStore {
    return MemoryManualUtilizationVarianceStore(
        mutableMapOf<String, ManualUtilizationVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualUtilizationVarianceStore(
    var inner: ManualUtilizationVarianceStore,
    var cache: TtlCache<ManualUtilizationVariance>,
    var hits: Int,
    var misses: Int,
) : ManualUtilizationVarianceStore {
    override fun find(code: String): ManualUtilizationVariance? {
        val hit = (cache.get(code) as ManualUtilizationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualUtilizationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualUtilizationVariance> = inner.list()
}

fun newCachedManualUtilizationVarianceStore(inner: ManualUtilizationVarianceStore, ttl: Int, clock: Clock): ManualUtilizationVarianceStore {
    return CachedManualUtilizationVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualUtilizationVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual utilization variances. Empty fields match all. */
data class ManualUtilizationVarianceQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualUtilizationVariance): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualUtilizationVariance>): MutableList<ManualUtilizationVariance> {
        val matched = filter(items) { candidate: ManualUtilizationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualUtilizationVariances(store: ManualUtilizationVarianceStore, query: ManualUtilizationVarianceQuery): MutableList<ManualUtilizationVariance> {
    return query.apply(store.list())
}
