package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict transit time digests by code. */
interface StrictTransitTimeDigestStore {
    fun find(code: String): StrictTransitTimeDigest?
    fun save(item: StrictTransitTimeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTransitTimeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTransitTimeDigestStore(
    var items: MutableMap<String, StrictTransitTimeDigest>,
    var order: MutableList<String>,
) : StrictTransitTimeDigestStore {
    override fun find(code: String): StrictTransitTimeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTransitTimeDigest) {
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

    override fun list(): MutableList<StrictTransitTimeDigest> {
        val out = mutableListOf<StrictTransitTimeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTransitTimeDigestStore(): StrictTransitTimeDigestStore {
    return MemoryStrictTransitTimeDigestStore(
        mutableMapOf<String, StrictTransitTimeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTransitTimeDigestStore(
    var inner: StrictTransitTimeDigestStore,
    var cache: TtlCache<StrictTransitTimeDigest>,
    var hits: Int,
    var misses: Int,
) : StrictTransitTimeDigestStore {
    override fun find(code: String): StrictTransitTimeDigest? {
        val hit = (cache.get(code) as StrictTransitTimeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTransitTimeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTransitTimeDigest> = inner.list()
}

fun newCachedStrictTransitTimeDigestStore(inner: StrictTransitTimeDigestStore, ttl: Int, clock: Clock): StrictTransitTimeDigestStore {
    return CachedStrictTransitTimeDigestStore(
        inner = inner,
        cache = newTtlCache<StrictTransitTimeDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict transit time digests. Empty fields match all. */
data class StrictTransitTimeDigestQuery(
    var locale: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTransitTimeDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTransitTimeDigest>): MutableList<StrictTransitTimeDigest> {
        val matched = filter(items) { candidate: StrictTransitTimeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTransitTimeDigests(store: StrictTransitTimeDigestStore, query: StrictTransitTimeDigestQuery): MutableList<StrictTransitTimeDigest> {
    return query.apply(store.list())
}
