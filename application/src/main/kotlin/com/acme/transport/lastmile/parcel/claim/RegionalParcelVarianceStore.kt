package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel variances by code. */
interface RegionalParcelVarianceStore {
    fun find(code: String): RegionalParcelVariance?
    fun save(item: RegionalParcelVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelVarianceStore(
    var items: MutableMap<String, RegionalParcelVariance>,
    var order: MutableList<String>,
) : RegionalParcelVarianceStore {
    override fun find(code: String): RegionalParcelVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelVariance) {
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

    override fun list(): MutableList<RegionalParcelVariance> {
        val out = mutableListOf<RegionalParcelVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelVarianceStore(): RegionalParcelVarianceStore {
    return MemoryRegionalParcelVarianceStore(
        mutableMapOf<String, RegionalParcelVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelVarianceStore(
    var inner: RegionalParcelVarianceStore,
    var cache: TtlCache<RegionalParcelVariance>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelVarianceStore {
    override fun find(code: String): RegionalParcelVariance? {
        val hit = (cache.get(code) as RegionalParcelVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelVariance> = inner.list()
}

fun newCachedRegionalParcelVarianceStore(inner: RegionalParcelVarianceStore, ttl: Int, clock: Clock): RegionalParcelVarianceStore {
    return CachedRegionalParcelVarianceStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel variances. Empty fields match all. */
data class RegionalParcelVarianceQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalParcelVariance>): MutableList<RegionalParcelVariance> {
        val matched = filter(items) { candidate: RegionalParcelVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelVariances(store: RegionalParcelVarianceStore, query: RegionalParcelVarianceQuery): MutableList<RegionalParcelVariance> {
    return query.apply(store.list())
}
