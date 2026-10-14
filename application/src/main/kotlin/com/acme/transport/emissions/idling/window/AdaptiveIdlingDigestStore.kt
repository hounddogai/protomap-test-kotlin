package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive idling digests by code. */
interface AdaptiveIdlingDigestStore {
    fun find(code: String): AdaptiveIdlingDigest?
    fun save(item: AdaptiveIdlingDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveIdlingDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveIdlingDigestStore(
    var items: MutableMap<String, AdaptiveIdlingDigest>,
    var order: MutableList<String>,
) : AdaptiveIdlingDigestStore {
    override fun find(code: String): AdaptiveIdlingDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveIdlingDigest) {
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

    override fun list(): MutableList<AdaptiveIdlingDigest> {
        val out = mutableListOf<AdaptiveIdlingDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveIdlingDigestStore(): AdaptiveIdlingDigestStore {
    return MemoryAdaptiveIdlingDigestStore(
        mutableMapOf<String, AdaptiveIdlingDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveIdlingDigestStore(
    var inner: AdaptiveIdlingDigestStore,
    var cache: TtlCache<AdaptiveIdlingDigest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveIdlingDigestStore {
    override fun find(code: String): AdaptiveIdlingDigest? {
        val hit = (cache.get(code) as AdaptiveIdlingDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveIdlingDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveIdlingDigest> = inner.list()
}

fun newCachedAdaptiveIdlingDigestStore(inner: AdaptiveIdlingDigestStore, ttl: Int, clock: Clock): AdaptiveIdlingDigestStore {
    return CachedAdaptiveIdlingDigestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveIdlingDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive idling digests. Empty fields match all. */
data class AdaptiveIdlingDigestQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveIdlingDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveIdlingDigest>): MutableList<AdaptiveIdlingDigest> {
        val matched = filter(items) { candidate: AdaptiveIdlingDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveIdlingDigests(store: AdaptiveIdlingDigestStore, query: AdaptiveIdlingDigestQuery): MutableList<AdaptiveIdlingDigest> {
    return query.apply(store.list())
}
