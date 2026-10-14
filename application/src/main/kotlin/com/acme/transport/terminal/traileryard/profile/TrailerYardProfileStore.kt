package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard profiles by code. */
interface TrailerYardProfileStore {
    fun find(code: String): TrailerYardProfile?
    fun save(item: TrailerYardProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardProfileStore(
    var items: MutableMap<String, TrailerYardProfile>,
    var order: MutableList<String>,
) : TrailerYardProfileStore {
    override fun find(code: String): TrailerYardProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardProfile) {
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

    override fun list(): MutableList<TrailerYardProfile> {
        val out = mutableListOf<TrailerYardProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardProfileStore(): TrailerYardProfileStore {
    return MemoryTrailerYardProfileStore(mutableMapOf<String, TrailerYardProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardProfileStore(
    var inner: TrailerYardProfileStore,
    var cache: TtlCache<TrailerYardProfile>,
    var hits: Int,
    var misses: Int,
) : TrailerYardProfileStore {
    override fun find(code: String): TrailerYardProfile? {
        val hit = (cache.get(code) as TrailerYardProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardProfile> = inner.list()
}

fun newCachedTrailerYardProfileStore(inner: TrailerYardProfileStore, ttl: Int, clock: Clock): TrailerYardProfileStore {
    return CachedTrailerYardProfileStore(
        inner = inner,
        cache = newTtlCache<TrailerYardProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard profiles. Empty fields match all. */
data class TrailerYardProfileQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardProfile>): MutableList<TrailerYardProfile> {
        val matched = filter(items) { candidate: TrailerYardProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardProfiles(store: TrailerYardProfileStore, query: TrailerYardProfileQuery): MutableList<TrailerYardProfile> {
    return query.apply(store.list())
}
