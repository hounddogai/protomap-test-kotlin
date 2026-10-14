package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered utilization profiles by code. */
interface TieredUtilizationProfileStore {
    fun find(code: String): TieredUtilizationProfile?
    fun save(item: TieredUtilizationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredUtilizationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredUtilizationProfileStore(
    var items: MutableMap<String, TieredUtilizationProfile>,
    var order: MutableList<String>,
) : TieredUtilizationProfileStore {
    override fun find(code: String): TieredUtilizationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredUtilizationProfile) {
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

    override fun list(): MutableList<TieredUtilizationProfile> {
        val out = mutableListOf<TieredUtilizationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredUtilizationProfileStore(): TieredUtilizationProfileStore {
    return MemoryTieredUtilizationProfileStore(
        mutableMapOf<String, TieredUtilizationProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredUtilizationProfileStore(
    var inner: TieredUtilizationProfileStore,
    var cache: TtlCache<TieredUtilizationProfile>,
    var hits: Int,
    var misses: Int,
) : TieredUtilizationProfileStore {
    override fun find(code: String): TieredUtilizationProfile? {
        val hit = (cache.get(code) as TieredUtilizationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredUtilizationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredUtilizationProfile> = inner.list()
}

fun newCachedTieredUtilizationProfileStore(inner: TieredUtilizationProfileStore, ttl: Int, clock: Clock): TieredUtilizationProfileStore {
    return CachedTieredUtilizationProfileStore(
        inner = inner,
        cache = newTtlCache<TieredUtilizationProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered utilization profiles. Empty fields match all. */
data class TieredUtilizationProfileQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredUtilizationProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredUtilizationProfile>): MutableList<TieredUtilizationProfile> {
        val matched = filter(items) { candidate: TieredUtilizationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredUtilizationProfiles(store: TieredUtilizationProfileStore, query: TieredUtilizationProfileQuery): MutableList<TieredUtilizationProfile> {
    return query.apply(store.list())
}
