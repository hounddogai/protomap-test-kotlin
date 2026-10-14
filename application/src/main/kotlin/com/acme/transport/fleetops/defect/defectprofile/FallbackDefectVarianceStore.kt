package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback defect variances by code. */
interface FallbackDefectVarianceStore {
    fun find(code: String): FallbackDefectVariance?
    fun save(item: FallbackDefectVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDefectVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDefectVarianceStore(
    var items: MutableMap<String, FallbackDefectVariance>,
    var order: MutableList<String>,
) : FallbackDefectVarianceStore {
    override fun find(code: String): FallbackDefectVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDefectVariance) {
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

    override fun list(): MutableList<FallbackDefectVariance> {
        val out = mutableListOf<FallbackDefectVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDefectVarianceStore(): FallbackDefectVarianceStore {
    return MemoryFallbackDefectVarianceStore(
        mutableMapOf<String, FallbackDefectVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDefectVarianceStore(
    var inner: FallbackDefectVarianceStore,
    var cache: TtlCache<FallbackDefectVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackDefectVarianceStore {
    override fun find(code: String): FallbackDefectVariance? {
        val hit = (cache.get(code) as FallbackDefectVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDefectVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDefectVariance> = inner.list()
}

fun newCachedFallbackDefectVarianceStore(inner: FallbackDefectVarianceStore, ttl: Int, clock: Clock): FallbackDefectVarianceStore {
    return CachedFallbackDefectVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackDefectVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback defect variances. Empty fields match all. */
data class FallbackDefectVarianceQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDefectVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDefectVariance>): MutableList<FallbackDefectVariance> {
        val matched = filter(items) { candidate: FallbackDefectVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDefectVariances(store: FallbackDefectVarianceStore, query: FallbackDefectVarianceQuery): MutableList<FallbackDefectVariance> {
    return query.apply(store.list())
}
