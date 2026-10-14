package com.acme.transport.crewing.availability.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability digests by code. */
interface AvailabilityDigestStore {
    fun find(code: String): AvailabilityDigest?
    fun save(item: AvailabilityDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityDigestStore(
    var items: MutableMap<String, AvailabilityDigest>,
    var order: MutableList<String>,
) : AvailabilityDigestStore {
    override fun find(code: String): AvailabilityDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityDigest) {
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

    override fun list(): MutableList<AvailabilityDigest> {
        val out = mutableListOf<AvailabilityDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityDigestStore(): AvailabilityDigestStore {
    return MemoryAvailabilityDigestStore(mutableMapOf<String, AvailabilityDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityDigestStore(
    var inner: AvailabilityDigestStore,
    var cache: TtlCache<AvailabilityDigest>,
    var hits: Int,
    var misses: Int,
) : AvailabilityDigestStore {
    override fun find(code: String): AvailabilityDigest? {
        val hit = (cache.get(code) as AvailabilityDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityDigest> = inner.list()
}

fun newCachedAvailabilityDigestStore(inner: AvailabilityDigestStore, ttl: Int, clock: Clock): AvailabilityDigestStore {
    return CachedAvailabilityDigestStore(
        inner = inner,
        cache = newTtlCache<AvailabilityDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability digests. Empty fields match all. */
data class AvailabilityDigestQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityDigest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityDigest>): MutableList<AvailabilityDigest> {
        val matched = filter(items) { candidate: AvailabilityDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityDigests(store: AvailabilityDigestStore, query: AvailabilityDigestQuery): MutableList<AvailabilityDigest> {
    return query.apply(store.list())
}
