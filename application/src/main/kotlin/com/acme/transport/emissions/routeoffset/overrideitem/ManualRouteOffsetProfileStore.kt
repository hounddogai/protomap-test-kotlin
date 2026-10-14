package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual route offset profiles by code. */
interface ManualRouteOffsetProfileStore {
    fun find(code: String): ManualRouteOffsetProfile?
    fun save(item: ManualRouteOffsetProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRouteOffsetProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRouteOffsetProfileStore(
    var items: MutableMap<String, ManualRouteOffsetProfile>,
    var order: MutableList<String>,
) : ManualRouteOffsetProfileStore {
    override fun find(code: String): ManualRouteOffsetProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRouteOffsetProfile) {
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

    override fun list(): MutableList<ManualRouteOffsetProfile> {
        val out = mutableListOf<ManualRouteOffsetProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRouteOffsetProfileStore(): ManualRouteOffsetProfileStore {
    return MemoryManualRouteOffsetProfileStore(
        mutableMapOf<String, ManualRouteOffsetProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRouteOffsetProfileStore(
    var inner: ManualRouteOffsetProfileStore,
    var cache: TtlCache<ManualRouteOffsetProfile>,
    var hits: Int,
    var misses: Int,
) : ManualRouteOffsetProfileStore {
    override fun find(code: String): ManualRouteOffsetProfile? {
        val hit = (cache.get(code) as ManualRouteOffsetProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRouteOffsetProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRouteOffsetProfile> = inner.list()
}

fun newCachedManualRouteOffsetProfileStore(inner: ManualRouteOffsetProfileStore, ttl: Int, clock: Clock): ManualRouteOffsetProfileStore {
    return CachedManualRouteOffsetProfileStore(
        inner = inner,
        cache = newTtlCache<ManualRouteOffsetProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual route offset profiles. Empty fields match all. */
data class ManualRouteOffsetProfileQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRouteOffsetProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRouteOffsetProfile>): MutableList<ManualRouteOffsetProfile> {
        val matched = filter(items) { candidate: ManualRouteOffsetProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRouteOffsetProfiles(store: ManualRouteOffsetProfileStore, query: ManualRouteOffsetProfileQuery): MutableList<ManualRouteOffsetProfile> {
    return query.apply(store.list())
}
