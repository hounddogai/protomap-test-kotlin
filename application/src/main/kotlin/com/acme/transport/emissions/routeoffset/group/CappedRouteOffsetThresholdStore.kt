package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped route offset thresholds by code. */
interface CappedRouteOffsetThresholdStore {
    fun find(code: String): CappedRouteOffsetThreshold?
    fun save(item: CappedRouteOffsetThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRouteOffsetThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRouteOffsetThresholdStore(
    var items: MutableMap<String, CappedRouteOffsetThreshold>,
    var order: MutableList<String>,
) : CappedRouteOffsetThresholdStore {
    override fun find(code: String): CappedRouteOffsetThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRouteOffsetThreshold) {
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

    override fun list(): MutableList<CappedRouteOffsetThreshold> {
        val out = mutableListOf<CappedRouteOffsetThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRouteOffsetThresholdStore(): CappedRouteOffsetThresholdStore {
    return MemoryCappedRouteOffsetThresholdStore(
        mutableMapOf<String, CappedRouteOffsetThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRouteOffsetThresholdStore(
    var inner: CappedRouteOffsetThresholdStore,
    var cache: TtlCache<CappedRouteOffsetThreshold>,
    var hits: Int,
    var misses: Int,
) : CappedRouteOffsetThresholdStore {
    override fun find(code: String): CappedRouteOffsetThreshold? {
        val hit = (cache.get(code) as CappedRouteOffsetThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRouteOffsetThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRouteOffsetThreshold> = inner.list()
}

fun newCachedCappedRouteOffsetThresholdStore(inner: CappedRouteOffsetThresholdStore, ttl: Int, clock: Clock): CappedRouteOffsetThresholdStore {
    return CachedCappedRouteOffsetThresholdStore(
        inner = inner,
        cache = newTtlCache<CappedRouteOffsetThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped route offset thresholds. Empty fields match all. */
data class CappedRouteOffsetThresholdQuery(
    var region: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRouteOffsetThreshold): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRouteOffsetThreshold>): MutableList<CappedRouteOffsetThreshold> {
        val matched = filter(items) { candidate: CappedRouteOffsetThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRouteOffsetThresholds(store: CappedRouteOffsetThresholdStore, query: CappedRouteOffsetThresholdQuery): MutableList<CappedRouteOffsetThreshold> {
    return query.apply(store.list())
}
