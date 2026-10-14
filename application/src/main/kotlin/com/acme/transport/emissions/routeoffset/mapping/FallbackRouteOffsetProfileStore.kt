package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset profiles by code. */
interface FallbackRouteOffsetProfileStore {
    fun find(code: String): FallbackRouteOffsetProfile?
    fun save(item: FallbackRouteOffsetProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetProfileStore(
    var items: MutableMap<String, FallbackRouteOffsetProfile>,
    var order: MutableList<String>,
) : FallbackRouteOffsetProfileStore {
    override fun find(code: String): FallbackRouteOffsetProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetProfile) {
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

    override fun list(): MutableList<FallbackRouteOffsetProfile> {
        val out = mutableListOf<FallbackRouteOffsetProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetProfileStore(): FallbackRouteOffsetProfileStore {
    return MemoryFallbackRouteOffsetProfileStore(
        mutableMapOf<String, FallbackRouteOffsetProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetProfileStore(
    var inner: FallbackRouteOffsetProfileStore,
    var cache: TtlCache<FallbackRouteOffsetProfile>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetProfileStore {
    override fun find(code: String): FallbackRouteOffsetProfile? {
        val hit = (cache.get(code) as FallbackRouteOffsetProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetProfile> = inner.list()
}

fun newCachedFallbackRouteOffsetProfileStore(inner: FallbackRouteOffsetProfileStore, ttl: Int, clock: Clock): FallbackRouteOffsetProfileStore {
    return CachedFallbackRouteOffsetProfileStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset profiles. Empty fields match all. */
data class FallbackRouteOffsetProfileQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRouteOffsetProfile>): MutableList<FallbackRouteOffsetProfile> {
        val matched = filter(items) { candidate: FallbackRouteOffsetProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetProfiles(store: FallbackRouteOffsetProfileStore, query: FallbackRouteOffsetProfileQuery): MutableList<FallbackRouteOffsetProfile> {
    return query.apply(store.list())
}
