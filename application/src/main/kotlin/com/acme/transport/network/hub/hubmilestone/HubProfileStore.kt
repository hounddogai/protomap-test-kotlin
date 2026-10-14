package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub profiles by code. */
interface HubProfileStore {
    fun find(code: String): HubProfile?
    fun save(item: HubProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubProfileStore(
    var items: MutableMap<String, HubProfile>,
    var order: MutableList<String>,
) : HubProfileStore {
    override fun find(code: String): HubProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubProfile) {
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

    override fun list(): MutableList<HubProfile> {
        val out = mutableListOf<HubProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubProfileStore(): HubProfileStore {
    return MemoryHubProfileStore(mutableMapOf<String, HubProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubProfileStore(
    var inner: HubProfileStore,
    var cache: TtlCache<HubProfile>,
    var hits: Int,
    var misses: Int,
) : HubProfileStore {
    override fun find(code: String): HubProfile? {
        val hit = (cache.get(code) as HubProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubProfile> = inner.list()
}

fun newCachedHubProfileStore(inner: HubProfileStore, ttl: Int, clock: Clock): HubProfileStore {
    return CachedHubProfileStore(
        inner = inner,
        cache = newTtlCache<HubProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub profiles. Empty fields match all. */
data class HubProfileQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubProfile>): MutableList<HubProfile> {
        val matched = filter(items) { candidate: HubProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubProfiles(store: HubProfileStore, query: HubProfileQuery): MutableList<HubProfile> {
    return query.apply(store.list())
}
