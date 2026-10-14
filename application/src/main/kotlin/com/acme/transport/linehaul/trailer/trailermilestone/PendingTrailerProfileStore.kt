package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending trailer profiles by code. */
interface PendingTrailerProfileStore {
    fun find(code: String): PendingTrailerProfile?
    fun save(item: PendingTrailerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTrailerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTrailerProfileStore(
    var items: MutableMap<String, PendingTrailerProfile>,
    var order: MutableList<String>,
) : PendingTrailerProfileStore {
    override fun find(code: String): PendingTrailerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTrailerProfile) {
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

    override fun list(): MutableList<PendingTrailerProfile> {
        val out = mutableListOf<PendingTrailerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTrailerProfileStore(): PendingTrailerProfileStore {
    return MemoryPendingTrailerProfileStore(
        mutableMapOf<String, PendingTrailerProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTrailerProfileStore(
    var inner: PendingTrailerProfileStore,
    var cache: TtlCache<PendingTrailerProfile>,
    var hits: Int,
    var misses: Int,
) : PendingTrailerProfileStore {
    override fun find(code: String): PendingTrailerProfile? {
        val hit = (cache.get(code) as PendingTrailerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTrailerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTrailerProfile> = inner.list()
}

fun newCachedPendingTrailerProfileStore(inner: PendingTrailerProfileStore, ttl: Int, clock: Clock): PendingTrailerProfileStore {
    return CachedPendingTrailerProfileStore(
        inner = inner,
        cache = newTtlCache<PendingTrailerProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending trailer profiles. Empty fields match all. */
data class PendingTrailerProfileQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTrailerProfile): Boolean {
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

    fun apply(items: MutableList<PendingTrailerProfile>): MutableList<PendingTrailerProfile> {
        val matched = filter(items) { candidate: PendingTrailerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTrailerProfiles(store: PendingTrailerProfileStore, query: PendingTrailerProfileQuery): MutableList<PendingTrailerProfile> {
    return query.apply(store.list())
}
