package com.acme.transport.customs.broker.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional broker variances by code. */
interface RegionalBrokerVarianceStore {
    fun find(code: String): RegionalBrokerVariance?
    fun save(item: RegionalBrokerVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBrokerVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBrokerVarianceStore(
    var items: MutableMap<String, RegionalBrokerVariance>,
    var order: MutableList<String>,
) : RegionalBrokerVarianceStore {
    override fun find(code: String): RegionalBrokerVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBrokerVariance) {
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

    override fun list(): MutableList<RegionalBrokerVariance> {
        val out = mutableListOf<RegionalBrokerVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBrokerVarianceStore(): RegionalBrokerVarianceStore {
    return MemoryRegionalBrokerVarianceStore(
        mutableMapOf<String, RegionalBrokerVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBrokerVarianceStore(
    var inner: RegionalBrokerVarianceStore,
    var cache: TtlCache<RegionalBrokerVariance>,
    var hits: Int,
    var misses: Int,
) : RegionalBrokerVarianceStore {
    override fun find(code: String): RegionalBrokerVariance? {
        val hit = (cache.get(code) as RegionalBrokerVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBrokerVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBrokerVariance> = inner.list()
}

fun newCachedRegionalBrokerVarianceStore(inner: RegionalBrokerVarianceStore, ttl: Int, clock: Clock): RegionalBrokerVarianceStore {
    return CachedRegionalBrokerVarianceStore(
        inner = inner,
        cache = newTtlCache<RegionalBrokerVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional broker variances. Empty fields match all. */
data class RegionalBrokerVarianceQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBrokerVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalBrokerVariance>): MutableList<RegionalBrokerVariance> {
        val matched = filter(items) { candidate: RegionalBrokerVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBrokerVariances(store: RegionalBrokerVarianceStore, query: RegionalBrokerVarianceQuery): MutableList<RegionalBrokerVariance> {
    return query.apply(store.list())
}
