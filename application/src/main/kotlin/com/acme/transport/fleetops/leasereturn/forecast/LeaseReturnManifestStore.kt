package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return manifests by code. */
interface LeaseReturnManifestStore {
    fun find(code: String): LeaseReturnManifest?
    fun save(item: LeaseReturnManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnManifestStore(
    var items: MutableMap<String, LeaseReturnManifest>,
    var order: MutableList<String>,
) : LeaseReturnManifestStore {
    override fun find(code: String): LeaseReturnManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnManifest) {
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

    override fun list(): MutableList<LeaseReturnManifest> {
        val out = mutableListOf<LeaseReturnManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnManifestStore(): LeaseReturnManifestStore {
    return MemoryLeaseReturnManifestStore(mutableMapOf<String, LeaseReturnManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnManifestStore(
    var inner: LeaseReturnManifestStore,
    var cache: TtlCache<LeaseReturnManifest>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnManifestStore {
    override fun find(code: String): LeaseReturnManifest? {
        val hit = (cache.get(code) as LeaseReturnManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnManifest> = inner.list()
}

fun newCachedLeaseReturnManifestStore(inner: LeaseReturnManifestStore, ttl: Int, clock: Clock): LeaseReturnManifestStore {
    return CachedLeaseReturnManifestStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return manifests. Empty fields match all. */
data class LeaseReturnManifestQuery(
    var country: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LeaseReturnManifest>): MutableList<LeaseReturnManifest> {
        val matched = filter(items) { candidate: LeaseReturnManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnManifests(store: LeaseReturnManifestStore, query: LeaseReturnManifestQuery): MutableList<LeaseReturnManifest> {
    return query.apply(store.list())
}
