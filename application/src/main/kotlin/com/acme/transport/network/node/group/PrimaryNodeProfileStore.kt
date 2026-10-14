package com.acme.transport.network.node.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary node profiles by code. */
interface PrimaryNodeProfileStore {
    fun find(code: String): PrimaryNodeProfile?
    fun save(item: PrimaryNodeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNodeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNodeProfileStore(
    var items: MutableMap<String, PrimaryNodeProfile>,
    var order: MutableList<String>,
) : PrimaryNodeProfileStore {
    override fun find(code: String): PrimaryNodeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNodeProfile) {
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

    override fun list(): MutableList<PrimaryNodeProfile> {
        val out = mutableListOf<PrimaryNodeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNodeProfileStore(): PrimaryNodeProfileStore {
    return MemoryPrimaryNodeProfileStore(mutableMapOf<String, PrimaryNodeProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNodeProfileStore(
    var inner: PrimaryNodeProfileStore,
    var cache: TtlCache<PrimaryNodeProfile>,
    var hits: Int,
    var misses: Int,
) : PrimaryNodeProfileStore {
    override fun find(code: String): PrimaryNodeProfile? {
        val hit = (cache.get(code) as PrimaryNodeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNodeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNodeProfile> = inner.list()
}

fun newCachedPrimaryNodeProfileStore(inner: PrimaryNodeProfileStore, ttl: Int, clock: Clock): PrimaryNodeProfileStore {
    return CachedPrimaryNodeProfileStore(
        inner = inner,
        cache = newTtlCache<PrimaryNodeProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary node profiles. Empty fields match all. */
data class PrimaryNodeProfileQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNodeProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryNodeProfile>): MutableList<PrimaryNodeProfile> {
        val matched = filter(items) { candidate: PrimaryNodeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNodeProfiles(store: PrimaryNodeProfileStore, query: PrimaryNodeProfileQuery): MutableList<PrimaryNodeProfile> {
    return query.apply(store.list())
}
