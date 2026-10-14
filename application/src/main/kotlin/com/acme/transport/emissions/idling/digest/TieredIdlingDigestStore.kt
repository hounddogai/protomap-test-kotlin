package com.acme.transport.emissions.idling.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered idling digests by code. */
interface TieredIdlingDigestStore {
    fun find(code: String): TieredIdlingDigest?
    fun save(item: TieredIdlingDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIdlingDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIdlingDigestStore(
    var items: MutableMap<String, TieredIdlingDigest>,
    var order: MutableList<String>,
) : TieredIdlingDigestStore {
    override fun find(code: String): TieredIdlingDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIdlingDigest) {
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

    override fun list(): MutableList<TieredIdlingDigest> {
        val out = mutableListOf<TieredIdlingDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIdlingDigestStore(): TieredIdlingDigestStore {
    return MemoryTieredIdlingDigestStore(mutableMapOf<String, TieredIdlingDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIdlingDigestStore(
    var inner: TieredIdlingDigestStore,
    var cache: TtlCache<TieredIdlingDigest>,
    var hits: Int,
    var misses: Int,
) : TieredIdlingDigestStore {
    override fun find(code: String): TieredIdlingDigest? {
        val hit = (cache.get(code) as TieredIdlingDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIdlingDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIdlingDigest> = inner.list()
}

fun newCachedTieredIdlingDigestStore(inner: TieredIdlingDigestStore, ttl: Int, clock: Clock): TieredIdlingDigestStore {
    return CachedTieredIdlingDigestStore(
        inner = inner,
        cache = newTtlCache<TieredIdlingDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered idling digests. Empty fields match all. */
data class TieredIdlingDigestQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIdlingDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<TieredIdlingDigest>): MutableList<TieredIdlingDigest> {
        val matched = filter(items) { candidate: TieredIdlingDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIdlingDigests(store: TieredIdlingDigestStore, query: TieredIdlingDigestQuery): MutableList<TieredIdlingDigest> {
    return query.apply(store.list())
}
