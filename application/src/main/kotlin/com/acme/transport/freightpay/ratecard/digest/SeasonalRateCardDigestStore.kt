package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal rate card digests by code. */
interface SeasonalRateCardDigestStore {
    fun find(code: String): SeasonalRateCardDigest?
    fun save(item: SeasonalRateCardDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRateCardDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRateCardDigestStore(
    var items: MutableMap<String, SeasonalRateCardDigest>,
    var order: MutableList<String>,
) : SeasonalRateCardDigestStore {
    override fun find(code: String): SeasonalRateCardDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRateCardDigest) {
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

    override fun list(): MutableList<SeasonalRateCardDigest> {
        val out = mutableListOf<SeasonalRateCardDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRateCardDigestStore(): SeasonalRateCardDigestStore {
    return MemorySeasonalRateCardDigestStore(
        mutableMapOf<String, SeasonalRateCardDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRateCardDigestStore(
    var inner: SeasonalRateCardDigestStore,
    var cache: TtlCache<SeasonalRateCardDigest>,
    var hits: Int,
    var misses: Int,
) : SeasonalRateCardDigestStore {
    override fun find(code: String): SeasonalRateCardDigest? {
        val hit = (cache.get(code) as SeasonalRateCardDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRateCardDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRateCardDigest> = inner.list()
}

fun newCachedSeasonalRateCardDigestStore(inner: SeasonalRateCardDigestStore, ttl: Int, clock: Clock): SeasonalRateCardDigestStore {
    return CachedSeasonalRateCardDigestStore(
        inner = inner,
        cache = newTtlCache<SeasonalRateCardDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal rate card digests. Empty fields match all. */
data class SeasonalRateCardDigestQuery(
    var locale: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRateCardDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRateCardDigest>): MutableList<SeasonalRateCardDigest> {
        val matched = filter(items) { candidate: SeasonalRateCardDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRateCardDigests(store: SeasonalRateCardDigestStore, query: SeasonalRateCardDigestQuery): MutableList<SeasonalRateCardDigest> {
    return query.apply(store.list())
}
