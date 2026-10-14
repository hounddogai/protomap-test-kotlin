package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization variances by code. */
interface UtilizationVarianceStore {
    fun find(code: String): UtilizationVariance?
    fun save(item: UtilizationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationVarianceStore(
    var items: MutableMap<String, UtilizationVariance>,
    var order: MutableList<String>,
) : UtilizationVarianceStore {
    override fun find(code: String): UtilizationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationVariance) {
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

    override fun list(): MutableList<UtilizationVariance> {
        val out = mutableListOf<UtilizationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationVarianceStore(): UtilizationVarianceStore {
    return MemoryUtilizationVarianceStore(mutableMapOf<String, UtilizationVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationVarianceStore(
    var inner: UtilizationVarianceStore,
    var cache: TtlCache<UtilizationVariance>,
    var hits: Int,
    var misses: Int,
) : UtilizationVarianceStore {
    override fun find(code: String): UtilizationVariance? {
        val hit = (cache.get(code) as UtilizationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationVariance> = inner.list()
}

fun newCachedUtilizationVarianceStore(inner: UtilizationVarianceStore, ttl: Int, clock: Clock): UtilizationVarianceStore {
    return CachedUtilizationVarianceStore(
        inner = inner,
        cache = newTtlCache<UtilizationVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization variances. Empty fields match all. */
data class UtilizationVarianceQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<UtilizationVariance>): MutableList<UtilizationVariance> {
        val matched = filter(items) { candidate: UtilizationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationVariances(store: UtilizationVarianceStore, query: UtilizationVarianceQuery): MutableList<UtilizationVariance> {
    return query.apply(store.list())
}
