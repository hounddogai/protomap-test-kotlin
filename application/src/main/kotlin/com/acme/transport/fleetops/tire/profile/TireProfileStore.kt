package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire profiles by code. */
interface TireProfileStore {
    fun find(code: String): TireProfile?
    fun save(item: TireProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireProfileStore(
    var items: MutableMap<String, TireProfile>,
    var order: MutableList<String>,
) : TireProfileStore {
    override fun find(code: String): TireProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireProfile) {
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

    override fun list(): MutableList<TireProfile> {
        val out = mutableListOf<TireProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireProfileStore(): TireProfileStore {
    return MemoryTireProfileStore(mutableMapOf<String, TireProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireProfileStore(
    var inner: TireProfileStore,
    var cache: TtlCache<TireProfile>,
    var hits: Int,
    var misses: Int,
) : TireProfileStore {
    override fun find(code: String): TireProfile? {
        val hit = (cache.get(code) as TireProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireProfile> = inner.list()
}

fun newCachedTireProfileStore(inner: TireProfileStore, ttl: Int, clock: Clock): TireProfileStore {
    return CachedTireProfileStore(
        inner = inner,
        cache = newTtlCache<TireProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire profiles. Empty fields match all. */
data class TireProfileQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireProfile>): MutableList<TireProfile> {
        val matched = filter(items) { candidate: TireProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireProfiles(store: TireProfileStore, query: TireProfileQuery): MutableList<TireProfile> {
    return query.apply(store.list())
}
