package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive lease return digests by code. */
interface AdaptiveLeaseReturnDigestStore {
    fun find(code: String): AdaptiveLeaseReturnDigest?
    fun save(item: AdaptiveLeaseReturnDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLeaseReturnDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLeaseReturnDigestStore(
    var items: MutableMap<String, AdaptiveLeaseReturnDigest>,
    var order: MutableList<String>,
) : AdaptiveLeaseReturnDigestStore {
    override fun find(code: String): AdaptiveLeaseReturnDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLeaseReturnDigest) {
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

    override fun list(): MutableList<AdaptiveLeaseReturnDigest> {
        val out = mutableListOf<AdaptiveLeaseReturnDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLeaseReturnDigestStore(): AdaptiveLeaseReturnDigestStore {
    return MemoryAdaptiveLeaseReturnDigestStore(
        mutableMapOf<String, AdaptiveLeaseReturnDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLeaseReturnDigestStore(
    var inner: AdaptiveLeaseReturnDigestStore,
    var cache: TtlCache<AdaptiveLeaseReturnDigest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLeaseReturnDigestStore {
    override fun find(code: String): AdaptiveLeaseReturnDigest? {
        val hit = (cache.get(code) as AdaptiveLeaseReturnDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLeaseReturnDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLeaseReturnDigest> = inner.list()
}

fun newCachedAdaptiveLeaseReturnDigestStore(inner: AdaptiveLeaseReturnDigestStore, ttl: Int, clock: Clock): AdaptiveLeaseReturnDigestStore {
    return CachedAdaptiveLeaseReturnDigestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLeaseReturnDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lease return digests. Empty fields match all. */
data class AdaptiveLeaseReturnDigestQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLeaseReturnDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLeaseReturnDigest>): MutableList<AdaptiveLeaseReturnDigest> {
        val matched = filter(items) { candidate: AdaptiveLeaseReturnDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLeaseReturnDigests(store: AdaptiveLeaseReturnDigestStore, query: AdaptiveLeaseReturnDigestQuery): MutableList<AdaptiveLeaseReturnDigest> {
    return query.apply(store.list())
}
