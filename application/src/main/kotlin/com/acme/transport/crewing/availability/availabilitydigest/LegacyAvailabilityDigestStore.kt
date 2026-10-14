package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy availability digests by code. */
interface LegacyAvailabilityDigestStore {
    fun find(code: String): LegacyAvailabilityDigest?
    fun save(item: LegacyAvailabilityDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAvailabilityDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAvailabilityDigestStore(
    var items: MutableMap<String, LegacyAvailabilityDigest>,
    var order: MutableList<String>,
) : LegacyAvailabilityDigestStore {
    override fun find(code: String): LegacyAvailabilityDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAvailabilityDigest) {
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

    override fun list(): MutableList<LegacyAvailabilityDigest> {
        val out = mutableListOf<LegacyAvailabilityDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAvailabilityDigestStore(): LegacyAvailabilityDigestStore {
    return MemoryLegacyAvailabilityDigestStore(
        mutableMapOf<String, LegacyAvailabilityDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAvailabilityDigestStore(
    var inner: LegacyAvailabilityDigestStore,
    var cache: TtlCache<LegacyAvailabilityDigest>,
    var hits: Int,
    var misses: Int,
) : LegacyAvailabilityDigestStore {
    override fun find(code: String): LegacyAvailabilityDigest? {
        val hit = (cache.get(code) as LegacyAvailabilityDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAvailabilityDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAvailabilityDigest> = inner.list()
}

fun newCachedLegacyAvailabilityDigestStore(inner: LegacyAvailabilityDigestStore, ttl: Int, clock: Clock): LegacyAvailabilityDigestStore {
    return CachedLegacyAvailabilityDigestStore(
        inner = inner,
        cache = newTtlCache<LegacyAvailabilityDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy availability digests. Empty fields match all. */
data class LegacyAvailabilityDigestQuery(
    var depot: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAvailabilityDigest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAvailabilityDigest>): MutableList<LegacyAvailabilityDigest> {
        val matched = filter(items) { candidate: LegacyAvailabilityDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAvailabilityDigests(store: LegacyAvailabilityDigestStore, query: LegacyAvailabilityDigestQuery): MutableList<LegacyAvailabilityDigest> {
    return query.apply(store.list())
}
