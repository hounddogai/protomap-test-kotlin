package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive relay digests by code. */
interface AdaptiveRelayDigestStore {
    fun find(code: String): AdaptiveRelayDigest?
    fun save(item: AdaptiveRelayDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRelayDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRelayDigestStore(
    var items: MutableMap<String, AdaptiveRelayDigest>,
    var order: MutableList<String>,
) : AdaptiveRelayDigestStore {
    override fun find(code: String): AdaptiveRelayDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRelayDigest) {
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

    override fun list(): MutableList<AdaptiveRelayDigest> {
        val out = mutableListOf<AdaptiveRelayDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRelayDigestStore(): AdaptiveRelayDigestStore {
    return MemoryAdaptiveRelayDigestStore(mutableMapOf<String, AdaptiveRelayDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRelayDigestStore(
    var inner: AdaptiveRelayDigestStore,
    var cache: TtlCache<AdaptiveRelayDigest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRelayDigestStore {
    override fun find(code: String): AdaptiveRelayDigest? {
        val hit = (cache.get(code) as AdaptiveRelayDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRelayDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRelayDigest> = inner.list()
}

fun newCachedAdaptiveRelayDigestStore(inner: AdaptiveRelayDigestStore, ttl: Int, clock: Clock): AdaptiveRelayDigestStore {
    return CachedAdaptiveRelayDigestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRelayDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive relay digests. Empty fields match all. */
data class AdaptiveRelayDigestQuery(
    var region: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRelayDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<AdaptiveRelayDigest>): MutableList<AdaptiveRelayDigest> {
        val matched = filter(items) { candidate: AdaptiveRelayDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRelayDigests(store: AdaptiveRelayDigestStore, query: AdaptiveRelayDigestQuery): MutableList<AdaptiveRelayDigest> {
    return query.apply(store.list())
}
