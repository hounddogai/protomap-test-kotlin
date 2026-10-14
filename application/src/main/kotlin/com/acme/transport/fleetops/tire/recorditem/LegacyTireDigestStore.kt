package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tire digests by code. */
interface LegacyTireDigestStore {
    fun find(code: String): LegacyTireDigest?
    fun save(item: LegacyTireDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTireDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTireDigestStore(
    var items: MutableMap<String, LegacyTireDigest>,
    var order: MutableList<String>,
) : LegacyTireDigestStore {
    override fun find(code: String): LegacyTireDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTireDigest) {
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

    override fun list(): MutableList<LegacyTireDigest> {
        val out = mutableListOf<LegacyTireDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTireDigestStore(): LegacyTireDigestStore {
    return MemoryLegacyTireDigestStore(mutableMapOf<String, LegacyTireDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTireDigestStore(
    var inner: LegacyTireDigestStore,
    var cache: TtlCache<LegacyTireDigest>,
    var hits: Int,
    var misses: Int,
) : LegacyTireDigestStore {
    override fun find(code: String): LegacyTireDigest? {
        val hit = (cache.get(code) as LegacyTireDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTireDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTireDigest> = inner.list()
}

fun newCachedLegacyTireDigestStore(inner: LegacyTireDigestStore, ttl: Int, clock: Clock): LegacyTireDigestStore {
    return CachedLegacyTireDigestStore(
        inner = inner,
        cache = newTtlCache<LegacyTireDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tire digests. Empty fields match all. */
data class LegacyTireDigestQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTireDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LegacyTireDigest>): MutableList<LegacyTireDigest> {
        val matched = filter(items) { candidate: LegacyTireDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTireDigests(store: LegacyTireDigestStore, query: LegacyTireDigestQuery): MutableList<LegacyTireDigest> {
    return query.apply(store.list())
}
