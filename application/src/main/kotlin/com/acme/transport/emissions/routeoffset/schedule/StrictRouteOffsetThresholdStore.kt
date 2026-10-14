package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict route offset thresholds by code. */
interface StrictRouteOffsetThresholdStore {
    fun find(code: String): StrictRouteOffsetThreshold?
    fun save(item: StrictRouteOffsetThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRouteOffsetThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRouteOffsetThresholdStore(
    var items: MutableMap<String, StrictRouteOffsetThreshold>,
    var order: MutableList<String>,
) : StrictRouteOffsetThresholdStore {
    override fun find(code: String): StrictRouteOffsetThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRouteOffsetThreshold) {
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

    override fun list(): MutableList<StrictRouteOffsetThreshold> {
        val out = mutableListOf<StrictRouteOffsetThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRouteOffsetThresholdStore(): StrictRouteOffsetThresholdStore {
    return MemoryStrictRouteOffsetThresholdStore(
        mutableMapOf<String, StrictRouteOffsetThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRouteOffsetThresholdStore(
    var inner: StrictRouteOffsetThresholdStore,
    var cache: TtlCache<StrictRouteOffsetThreshold>,
    var hits: Int,
    var misses: Int,
) : StrictRouteOffsetThresholdStore {
    override fun find(code: String): StrictRouteOffsetThreshold? {
        val hit = (cache.get(code) as StrictRouteOffsetThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRouteOffsetThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRouteOffsetThreshold> = inner.list()
}

fun newCachedStrictRouteOffsetThresholdStore(inner: StrictRouteOffsetThresholdStore, ttl: Int, clock: Clock): StrictRouteOffsetThresholdStore {
    return CachedStrictRouteOffsetThresholdStore(
        inner = inner,
        cache = newTtlCache<StrictRouteOffsetThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict route offset thresholds. Empty fields match all. */
data class StrictRouteOffsetThresholdQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRouteOffsetThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictRouteOffsetThreshold>): MutableList<StrictRouteOffsetThreshold> {
        val matched = filter(items) { candidate: StrictRouteOffsetThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRouteOffsetThresholds(store: StrictRouteOffsetThresholdStore, query: StrictRouteOffsetThresholdQuery): MutableList<StrictRouteOffsetThreshold> {
    return query.apply(store.list())
}
