package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel variances by code. */
interface ParcelVarianceStore {
    fun find(code: String): ParcelVariance?
    fun save(item: ParcelVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelVarianceStore(
    var items: MutableMap<String, ParcelVariance>,
    var order: MutableList<String>,
) : ParcelVarianceStore {
    override fun find(code: String): ParcelVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelVariance) {
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

    override fun list(): MutableList<ParcelVariance> {
        val out = mutableListOf<ParcelVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelVarianceStore(): ParcelVarianceStore {
    return MemoryParcelVarianceStore(mutableMapOf<String, ParcelVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelVarianceStore(
    var inner: ParcelVarianceStore,
    var cache: TtlCache<ParcelVariance>,
    var hits: Int,
    var misses: Int,
) : ParcelVarianceStore {
    override fun find(code: String): ParcelVariance? {
        val hit = (cache.get(code) as ParcelVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelVariance> = inner.list()
}

fun newCachedParcelVarianceStore(inner: ParcelVarianceStore, ttl: Int, clock: Clock): ParcelVarianceStore {
    return CachedParcelVarianceStore(
        inner = inner,
        cache = newTtlCache<ParcelVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel variances. Empty fields match all. */
data class ParcelVarianceQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelVariance>): MutableList<ParcelVariance> {
        val matched = filter(items) { candidate: ParcelVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelVariances(store: ParcelVarianceStore, query: ParcelVarianceQuery): MutableList<ParcelVariance> {
    return query.apply(store.list())
}
