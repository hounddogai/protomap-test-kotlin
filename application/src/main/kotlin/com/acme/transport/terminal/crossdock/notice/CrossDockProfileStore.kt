package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock profiles by code. */
interface CrossDockProfileStore {
    fun find(code: String): CrossDockProfile?
    fun save(item: CrossDockProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockProfileStore(
    var items: MutableMap<String, CrossDockProfile>,
    var order: MutableList<String>,
) : CrossDockProfileStore {
    override fun find(code: String): CrossDockProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockProfile) {
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

    override fun list(): MutableList<CrossDockProfile> {
        val out = mutableListOf<CrossDockProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockProfileStore(): CrossDockProfileStore {
    return MemoryCrossDockProfileStore(mutableMapOf<String, CrossDockProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockProfileStore(
    var inner: CrossDockProfileStore,
    var cache: TtlCache<CrossDockProfile>,
    var hits: Int,
    var misses: Int,
) : CrossDockProfileStore {
    override fun find(code: String): CrossDockProfile? {
        val hit = (cache.get(code) as CrossDockProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockProfile> = inner.list()
}

fun newCachedCrossDockProfileStore(inner: CrossDockProfileStore, ttl: Int, clock: Clock): CrossDockProfileStore {
    return CachedCrossDockProfileStore(
        inner = inner,
        cache = newTtlCache<CrossDockProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock profiles. Empty fields match all. */
data class CrossDockProfileQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockProfile): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockProfile>): MutableList<CrossDockProfile> {
        val matched = filter(items) { candidate: CrossDockProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockProfiles(store: CrossDockProfileStore, query: CrossDockProfileQuery): MutableList<CrossDockProfile> {
    return query.apply(store.list())
}
