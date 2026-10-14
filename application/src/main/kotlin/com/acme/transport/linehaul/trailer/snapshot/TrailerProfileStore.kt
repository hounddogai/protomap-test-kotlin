package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer profiles by code. */
interface TrailerProfileStore {
    fun find(code: String): TrailerProfile?
    fun save(item: TrailerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerProfileStore(
    var items: MutableMap<String, TrailerProfile>,
    var order: MutableList<String>,
) : TrailerProfileStore {
    override fun find(code: String): TrailerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerProfile) {
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

    override fun list(): MutableList<TrailerProfile> {
        val out = mutableListOf<TrailerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerProfileStore(): TrailerProfileStore {
    return MemoryTrailerProfileStore(mutableMapOf<String, TrailerProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerProfileStore(
    var inner: TrailerProfileStore,
    var cache: TtlCache<TrailerProfile>,
    var hits: Int,
    var misses: Int,
) : TrailerProfileStore {
    override fun find(code: String): TrailerProfile? {
        val hit = (cache.get(code) as TrailerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerProfile> = inner.list()
}

fun newCachedTrailerProfileStore(inner: TrailerProfileStore, ttl: Int, clock: Clock): TrailerProfileStore {
    return CachedTrailerProfileStore(
        inner = inner,
        cache = newTtlCache<TrailerProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer profiles. Empty fields match all. */
data class TrailerProfileQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerProfile): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TrailerProfile>): MutableList<TrailerProfile> {
        val matched = filter(items) { candidate: TrailerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerProfiles(store: TrailerProfileStore, query: TrailerProfileQuery): MutableList<TrailerProfile> {
    return query.apply(store.list())
}
