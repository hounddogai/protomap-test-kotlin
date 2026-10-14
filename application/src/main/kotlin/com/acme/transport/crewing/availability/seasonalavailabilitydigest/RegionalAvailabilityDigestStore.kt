package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional availability digests by code. */
interface RegionalAvailabilityDigestStore {
    fun find(code: String): RegionalAvailabilityDigest?
    fun save(item: RegionalAvailabilityDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAvailabilityDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAvailabilityDigestStore(
    var items: MutableMap<String, RegionalAvailabilityDigest>,
    var order: MutableList<String>,
) : RegionalAvailabilityDigestStore {
    override fun find(code: String): RegionalAvailabilityDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAvailabilityDigest) {
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

    override fun list(): MutableList<RegionalAvailabilityDigest> {
        val out = mutableListOf<RegionalAvailabilityDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAvailabilityDigestStore(): RegionalAvailabilityDigestStore {
    return MemoryRegionalAvailabilityDigestStore(
        mutableMapOf<String, RegionalAvailabilityDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAvailabilityDigestStore(
    var inner: RegionalAvailabilityDigestStore,
    var cache: TtlCache<RegionalAvailabilityDigest>,
    var hits: Int,
    var misses: Int,
) : RegionalAvailabilityDigestStore {
    override fun find(code: String): RegionalAvailabilityDigest? {
        val hit = (cache.get(code) as RegionalAvailabilityDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAvailabilityDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAvailabilityDigest> = inner.list()
}

fun newCachedRegionalAvailabilityDigestStore(inner: RegionalAvailabilityDigestStore, ttl: Int, clock: Clock): RegionalAvailabilityDigestStore {
    return CachedRegionalAvailabilityDigestStore(
        inner = inner,
        cache = newTtlCache<RegionalAvailabilityDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional availability digests. Empty fields match all. */
data class RegionalAvailabilityDigestQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAvailabilityDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAvailabilityDigest>): MutableList<RegionalAvailabilityDigest> {
        val matched = filter(items) { candidate: RegionalAvailabilityDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAvailabilityDigests(store: RegionalAvailabilityDigestStore, query: RegionalAvailabilityDigestQuery): MutableList<RegionalAvailabilityDigest> {
    return query.apply(store.list())
}
