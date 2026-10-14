package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual route offset variances by code. */
interface ManualRouteOffsetVarianceStore {
    fun find(code: String): ManualRouteOffsetVariance?
    fun save(item: ManualRouteOffsetVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRouteOffsetVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRouteOffsetVarianceStore(
    var items: MutableMap<String, ManualRouteOffsetVariance>,
    var order: MutableList<String>,
) : ManualRouteOffsetVarianceStore {
    override fun find(code: String): ManualRouteOffsetVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRouteOffsetVariance) {
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

    override fun list(): MutableList<ManualRouteOffsetVariance> {
        val out = mutableListOf<ManualRouteOffsetVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRouteOffsetVarianceStore(): ManualRouteOffsetVarianceStore {
    return MemoryManualRouteOffsetVarianceStore(
        mutableMapOf<String, ManualRouteOffsetVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRouteOffsetVarianceStore(
    var inner: ManualRouteOffsetVarianceStore,
    var cache: TtlCache<ManualRouteOffsetVariance>,
    var hits: Int,
    var misses: Int,
) : ManualRouteOffsetVarianceStore {
    override fun find(code: String): ManualRouteOffsetVariance? {
        val hit = (cache.get(code) as ManualRouteOffsetVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRouteOffsetVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRouteOffsetVariance> = inner.list()
}

fun newCachedManualRouteOffsetVarianceStore(inner: ManualRouteOffsetVarianceStore, ttl: Int, clock: Clock): ManualRouteOffsetVarianceStore {
    return CachedManualRouteOffsetVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualRouteOffsetVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual route offset variances. Empty fields match all. */
data class ManualRouteOffsetVarianceQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRouteOffsetVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRouteOffsetVariance>): MutableList<ManualRouteOffsetVariance> {
        val matched = filter(items) { candidate: ManualRouteOffsetVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRouteOffsetVariances(store: ManualRouteOffsetVarianceStore, query: ManualRouteOffsetVarianceQuery): MutableList<ManualRouteOffsetVariance> {
    return query.apply(store.list())
}
