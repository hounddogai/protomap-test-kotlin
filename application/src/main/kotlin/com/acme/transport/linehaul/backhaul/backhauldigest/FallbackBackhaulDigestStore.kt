package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback backhaul digests by code. */
interface FallbackBackhaulDigestStore {
    fun find(code: String): FallbackBackhaulDigest?
    fun save(item: FallbackBackhaulDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackBackhaulDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackBackhaulDigestStore(
    var items: MutableMap<String, FallbackBackhaulDigest>,
    var order: MutableList<String>,
) : FallbackBackhaulDigestStore {
    override fun find(code: String): FallbackBackhaulDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackBackhaulDigest) {
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

    override fun list(): MutableList<FallbackBackhaulDigest> {
        val out = mutableListOf<FallbackBackhaulDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackBackhaulDigestStore(): FallbackBackhaulDigestStore {
    return MemoryFallbackBackhaulDigestStore(
        mutableMapOf<String, FallbackBackhaulDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackBackhaulDigestStore(
    var inner: FallbackBackhaulDigestStore,
    var cache: TtlCache<FallbackBackhaulDigest>,
    var hits: Int,
    var misses: Int,
) : FallbackBackhaulDigestStore {
    override fun find(code: String): FallbackBackhaulDigest? {
        val hit = (cache.get(code) as FallbackBackhaulDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackBackhaulDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackBackhaulDigest> = inner.list()
}

fun newCachedFallbackBackhaulDigestStore(inner: FallbackBackhaulDigestStore, ttl: Int, clock: Clock): FallbackBackhaulDigestStore {
    return CachedFallbackBackhaulDigestStore(
        inner = inner,
        cache = newTtlCache<FallbackBackhaulDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback backhaul digests. Empty fields match all. */
data class FallbackBackhaulDigestQuery(
    var region: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackBackhaulDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackBackhaulDigest>): MutableList<FallbackBackhaulDigest> {
        val matched = filter(items) { candidate: FallbackBackhaulDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackBackhaulDigests(store: FallbackBackhaulDigestStore, query: FallbackBackhaulDigestQuery): MutableList<FallbackBackhaulDigest> {
    return query.apply(store.list())
}
