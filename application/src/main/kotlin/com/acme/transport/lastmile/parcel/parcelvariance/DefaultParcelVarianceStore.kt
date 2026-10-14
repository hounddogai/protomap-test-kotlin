package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel variances by code. */
interface DefaultParcelVarianceStore {
    fun find(code: String): DefaultParcelVariance?
    fun save(item: DefaultParcelVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelVarianceStore(
    var items: MutableMap<String, DefaultParcelVariance>,
    var order: MutableList<String>,
) : DefaultParcelVarianceStore {
    override fun find(code: String): DefaultParcelVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelVariance) {
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

    override fun list(): MutableList<DefaultParcelVariance> {
        val out = mutableListOf<DefaultParcelVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelVarianceStore(): DefaultParcelVarianceStore {
    return MemoryDefaultParcelVarianceStore(
        mutableMapOf<String, DefaultParcelVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelVarianceStore(
    var inner: DefaultParcelVarianceStore,
    var cache: TtlCache<DefaultParcelVariance>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelVarianceStore {
    override fun find(code: String): DefaultParcelVariance? {
        val hit = (cache.get(code) as DefaultParcelVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelVariance> = inner.list()
}

fun newCachedDefaultParcelVarianceStore(inner: DefaultParcelVarianceStore, ttl: Int, clock: Clock): DefaultParcelVarianceStore {
    return CachedDefaultParcelVarianceStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel variances. Empty fields match all. */
data class DefaultParcelVarianceQuery(
    var segment: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelVariance>): MutableList<DefaultParcelVariance> {
        val matched = filter(items) { candidate: DefaultParcelVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelVariances(store: DefaultParcelVarianceStore, query: DefaultParcelVarianceQuery): MutableList<DefaultParcelVariance> {
    return query.apply(store.list())
}
