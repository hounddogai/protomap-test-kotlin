package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary capacity variances by code. */
interface PrimaryCapacityVarianceStore {
    fun find(code: String): PrimaryCapacityVariance?
    fun save(item: PrimaryCapacityVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCapacityVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCapacityVarianceStore(
    var items: MutableMap<String, PrimaryCapacityVariance>,
    var order: MutableList<String>,
) : PrimaryCapacityVarianceStore {
    override fun find(code: String): PrimaryCapacityVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCapacityVariance) {
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

    override fun list(): MutableList<PrimaryCapacityVariance> {
        val out = mutableListOf<PrimaryCapacityVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCapacityVarianceStore(): PrimaryCapacityVarianceStore {
    return MemoryPrimaryCapacityVarianceStore(
        mutableMapOf<String, PrimaryCapacityVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCapacityVarianceStore(
    var inner: PrimaryCapacityVarianceStore,
    var cache: TtlCache<PrimaryCapacityVariance>,
    var hits: Int,
    var misses: Int,
) : PrimaryCapacityVarianceStore {
    override fun find(code: String): PrimaryCapacityVariance? {
        val hit = (cache.get(code) as PrimaryCapacityVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCapacityVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCapacityVariance> = inner.list()
}

fun newCachedPrimaryCapacityVarianceStore(inner: PrimaryCapacityVarianceStore, ttl: Int, clock: Clock): PrimaryCapacityVarianceStore {
    return CachedPrimaryCapacityVarianceStore(
        inner = inner,
        cache = newTtlCache<PrimaryCapacityVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary capacity variances. Empty fields match all. */
data class PrimaryCapacityVarianceQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCapacityVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCapacityVariance>): MutableList<PrimaryCapacityVariance> {
        val matched = filter(items) { candidate: PrimaryCapacityVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCapacityVariances(store: PrimaryCapacityVarianceStore, query: PrimaryCapacityVarianceQuery): MutableList<PrimaryCapacityVariance> {
    return query.apply(store.list())
}
