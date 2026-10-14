package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone variances by code. */
interface ZoneVarianceStore {
    fun find(code: String): ZoneVariance?
    fun save(item: ZoneVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneVarianceStore(
    var items: MutableMap<String, ZoneVariance>,
    var order: MutableList<String>,
) : ZoneVarianceStore {
    override fun find(code: String): ZoneVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneVariance) {
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

    override fun list(): MutableList<ZoneVariance> {
        val out = mutableListOf<ZoneVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneVarianceStore(): ZoneVarianceStore {
    return MemoryZoneVarianceStore(mutableMapOf<String, ZoneVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneVarianceStore(
    var inner: ZoneVarianceStore,
    var cache: TtlCache<ZoneVariance>,
    var hits: Int,
    var misses: Int,
) : ZoneVarianceStore {
    override fun find(code: String): ZoneVariance? {
        val hit = (cache.get(code) as ZoneVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneVariance> = inner.list()
}

fun newCachedZoneVarianceStore(inner: ZoneVarianceStore, ttl: Int, clock: Clock): ZoneVarianceStore {
    return CachedZoneVarianceStore(
        inner = inner,
        cache = newTtlCache<ZoneVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone variances. Empty fields match all. */
data class ZoneVarianceQuery(
    var region: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneVariance): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneVariance>): MutableList<ZoneVariance> {
        val matched = filter(items) { candidate: ZoneVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneVariances(store: ZoneVarianceStore, query: ZoneVarianceQuery): MutableList<ZoneVariance> {
    return query.apply(store.list())
}
