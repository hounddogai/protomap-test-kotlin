package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending route offset profiles by code. */
interface PendingRouteOffsetProfileStore {
    fun find(code: String): PendingRouteOffsetProfile?
    fun save(item: PendingRouteOffsetProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRouteOffsetProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRouteOffsetProfileStore(
    var items: MutableMap<String, PendingRouteOffsetProfile>,
    var order: MutableList<String>,
) : PendingRouteOffsetProfileStore {
    override fun find(code: String): PendingRouteOffsetProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRouteOffsetProfile) {
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

    override fun list(): MutableList<PendingRouteOffsetProfile> {
        val out = mutableListOf<PendingRouteOffsetProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRouteOffsetProfileStore(): PendingRouteOffsetProfileStore {
    return MemoryPendingRouteOffsetProfileStore(
        mutableMapOf<String, PendingRouteOffsetProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRouteOffsetProfileStore(
    var inner: PendingRouteOffsetProfileStore,
    var cache: TtlCache<PendingRouteOffsetProfile>,
    var hits: Int,
    var misses: Int,
) : PendingRouteOffsetProfileStore {
    override fun find(code: String): PendingRouteOffsetProfile? {
        val hit = (cache.get(code) as PendingRouteOffsetProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRouteOffsetProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRouteOffsetProfile> = inner.list()
}

fun newCachedPendingRouteOffsetProfileStore(inner: PendingRouteOffsetProfileStore, ttl: Int, clock: Clock): PendingRouteOffsetProfileStore {
    return CachedPendingRouteOffsetProfileStore(
        inner = inner,
        cache = newTtlCache<PendingRouteOffsetProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending route offset profiles. Empty fields match all. */
data class PendingRouteOffsetProfileQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRouteOffsetProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRouteOffsetProfile>): MutableList<PendingRouteOffsetProfile> {
        val matched = filter(items) { candidate: PendingRouteOffsetProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRouteOffsetProfiles(store: PendingRouteOffsetProfileStore, query: PendingRouteOffsetProfileQuery): MutableList<PendingRouteOffsetProfile> {
    return query.apply(store.list())
}
