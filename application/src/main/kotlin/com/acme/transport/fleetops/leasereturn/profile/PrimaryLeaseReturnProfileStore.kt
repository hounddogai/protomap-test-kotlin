package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary lease return profiles by code. */
interface PrimaryLeaseReturnProfileStore {
    fun find(code: String): PrimaryLeaseReturnProfile?
    fun save(item: PrimaryLeaseReturnProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLeaseReturnProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLeaseReturnProfileStore(
    var items: MutableMap<String, PrimaryLeaseReturnProfile>,
    var order: MutableList<String>,
) : PrimaryLeaseReturnProfileStore {
    override fun find(code: String): PrimaryLeaseReturnProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLeaseReturnProfile) {
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

    override fun list(): MutableList<PrimaryLeaseReturnProfile> {
        val out = mutableListOf<PrimaryLeaseReturnProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLeaseReturnProfileStore(): PrimaryLeaseReturnProfileStore {
    return MemoryPrimaryLeaseReturnProfileStore(
        mutableMapOf<String, PrimaryLeaseReturnProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLeaseReturnProfileStore(
    var inner: PrimaryLeaseReturnProfileStore,
    var cache: TtlCache<PrimaryLeaseReturnProfile>,
    var hits: Int,
    var misses: Int,
) : PrimaryLeaseReturnProfileStore {
    override fun find(code: String): PrimaryLeaseReturnProfile? {
        val hit = (cache.get(code) as PrimaryLeaseReturnProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLeaseReturnProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLeaseReturnProfile> = inner.list()
}

fun newCachedPrimaryLeaseReturnProfileStore(inner: PrimaryLeaseReturnProfileStore, ttl: Int, clock: Clock): PrimaryLeaseReturnProfileStore {
    return CachedPrimaryLeaseReturnProfileStore(
        inner = inner,
        cache = newTtlCache<PrimaryLeaseReturnProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary lease return profiles. Empty fields match all. */
data class PrimaryLeaseReturnProfileQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLeaseReturnProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryLeaseReturnProfile>): MutableList<PrimaryLeaseReturnProfile> {
        val matched = filter(items) { candidate: PrimaryLeaseReturnProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLeaseReturnProfiles(store: PrimaryLeaseReturnProfileStore, query: PrimaryLeaseReturnProfileQuery): MutableList<PrimaryLeaseReturnProfile> {
    return query.apply(store.list())
}
