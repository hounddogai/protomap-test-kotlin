package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone profiles by code. */
interface GlobalZoneProfileStore {
    fun find(code: String): GlobalZoneProfile?
    fun save(item: GlobalZoneProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneProfileStore(
    var items: MutableMap<String, GlobalZoneProfile>,
    var order: MutableList<String>,
) : GlobalZoneProfileStore {
    override fun find(code: String): GlobalZoneProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneProfile) {
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

    override fun list(): MutableList<GlobalZoneProfile> {
        val out = mutableListOf<GlobalZoneProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneProfileStore(): GlobalZoneProfileStore {
    return MemoryGlobalZoneProfileStore(mutableMapOf<String, GlobalZoneProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneProfileStore(
    var inner: GlobalZoneProfileStore,
    var cache: TtlCache<GlobalZoneProfile>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneProfileStore {
    override fun find(code: String): GlobalZoneProfile? {
        val hit = (cache.get(code) as GlobalZoneProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneProfile> = inner.list()
}

fun newCachedGlobalZoneProfileStore(inner: GlobalZoneProfileStore, ttl: Int, clock: Clock): GlobalZoneProfileStore {
    return CachedGlobalZoneProfileStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone profiles. Empty fields match all. */
data class GlobalZoneProfileQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneProfile): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalZoneProfile>): MutableList<GlobalZoneProfile> {
        val matched = filter(items) { candidate: GlobalZoneProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneProfiles(store: GlobalZoneProfileStore, query: GlobalZoneProfileQuery): MutableList<GlobalZoneProfile> {
    return query.apply(store.list())
}
