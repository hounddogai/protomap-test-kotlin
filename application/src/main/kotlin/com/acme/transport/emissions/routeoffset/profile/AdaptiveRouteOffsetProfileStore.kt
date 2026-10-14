package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive route offset profiles by code. */
interface AdaptiveRouteOffsetProfileStore {
    fun find(code: String): AdaptiveRouteOffsetProfile?
    fun save(item: AdaptiveRouteOffsetProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRouteOffsetProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRouteOffsetProfileStore(
    var items: MutableMap<String, AdaptiveRouteOffsetProfile>,
    var order: MutableList<String>,
) : AdaptiveRouteOffsetProfileStore {
    override fun find(code: String): AdaptiveRouteOffsetProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRouteOffsetProfile) {
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

    override fun list(): MutableList<AdaptiveRouteOffsetProfile> {
        val out = mutableListOf<AdaptiveRouteOffsetProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRouteOffsetProfileStore(): AdaptiveRouteOffsetProfileStore {
    return MemoryAdaptiveRouteOffsetProfileStore(
        mutableMapOf<String, AdaptiveRouteOffsetProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRouteOffsetProfileStore(
    var inner: AdaptiveRouteOffsetProfileStore,
    var cache: TtlCache<AdaptiveRouteOffsetProfile>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRouteOffsetProfileStore {
    override fun find(code: String): AdaptiveRouteOffsetProfile? {
        val hit = (cache.get(code) as AdaptiveRouteOffsetProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRouteOffsetProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRouteOffsetProfile> = inner.list()
}

fun newCachedAdaptiveRouteOffsetProfileStore(inner: AdaptiveRouteOffsetProfileStore, ttl: Int, clock: Clock): AdaptiveRouteOffsetProfileStore {
    return CachedAdaptiveRouteOffsetProfileStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRouteOffsetProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive route offset profiles. Empty fields match all. */
data class AdaptiveRouteOffsetProfileQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRouteOffsetProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRouteOffsetProfile>): MutableList<AdaptiveRouteOffsetProfile> {
        val matched = filter(items) { candidate: AdaptiveRouteOffsetProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRouteOffsetProfiles(store: AdaptiveRouteOffsetProfileStore, query: AdaptiveRouteOffsetProfileQuery): MutableList<AdaptiveRouteOffsetProfile> {
    return query.apply(store.list())
}
