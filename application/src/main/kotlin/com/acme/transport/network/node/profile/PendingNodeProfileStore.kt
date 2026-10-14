package com.acme.transport.network.node.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node profiles by code. */
interface PendingNodeProfileStore {
    fun find(code: String): PendingNodeProfile?
    fun save(item: PendingNodeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodeProfileStore(
    var items: MutableMap<String, PendingNodeProfile>,
    var order: MutableList<String>,
) : PendingNodeProfileStore {
    override fun find(code: String): PendingNodeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodeProfile) {
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

    override fun list(): MutableList<PendingNodeProfile> {
        val out = mutableListOf<PendingNodeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodeProfileStore(): PendingNodeProfileStore {
    return MemoryPendingNodeProfileStore(mutableMapOf<String, PendingNodeProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodeProfileStore(
    var inner: PendingNodeProfileStore,
    var cache: TtlCache<PendingNodeProfile>,
    var hits: Int,
    var misses: Int,
) : PendingNodeProfileStore {
    override fun find(code: String): PendingNodeProfile? {
        val hit = (cache.get(code) as PendingNodeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodeProfile> = inner.list()
}

fun newCachedPendingNodeProfileStore(inner: PendingNodeProfileStore, ttl: Int, clock: Clock): PendingNodeProfileStore {
    return CachedPendingNodeProfileStore(
        inner = inner,
        cache = newTtlCache<PendingNodeProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node profiles. Empty fields match all. */
data class PendingNodeProfileQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodeProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNodeProfile>): MutableList<PendingNodeProfile> {
        val matched = filter(items) { candidate: PendingNodeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodeProfiles(store: PendingNodeProfileStore, query: PendingNodeProfileQuery): MutableList<PendingNodeProfile> {
    return query.apply(store.list())
}
