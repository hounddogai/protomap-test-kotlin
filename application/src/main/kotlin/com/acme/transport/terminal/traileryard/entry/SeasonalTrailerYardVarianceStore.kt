package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard variances by code. */
interface SeasonalTrailerYardVarianceStore {
    fun find(code: String): SeasonalTrailerYardVariance?
    fun save(item: SeasonalTrailerYardVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardVarianceStore(
    var items: MutableMap<String, SeasonalTrailerYardVariance>,
    var order: MutableList<String>,
) : SeasonalTrailerYardVarianceStore {
    override fun find(code: String): SeasonalTrailerYardVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardVariance) {
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

    override fun list(): MutableList<SeasonalTrailerYardVariance> {
        val out = mutableListOf<SeasonalTrailerYardVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardVarianceStore(): SeasonalTrailerYardVarianceStore {
    return MemorySeasonalTrailerYardVarianceStore(
        mutableMapOf<String, SeasonalTrailerYardVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardVarianceStore(
    var inner: SeasonalTrailerYardVarianceStore,
    var cache: TtlCache<SeasonalTrailerYardVariance>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardVarianceStore {
    override fun find(code: String): SeasonalTrailerYardVariance? {
        val hit = (cache.get(code) as SeasonalTrailerYardVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardVariance> = inner.list()
}

fun newCachedSeasonalTrailerYardVarianceStore(inner: SeasonalTrailerYardVarianceStore, ttl: Int, clock: Clock): SeasonalTrailerYardVarianceStore {
    return CachedSeasonalTrailerYardVarianceStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard variances. Empty fields match all. */
data class SeasonalTrailerYardVarianceQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardVariance>): MutableList<SeasonalTrailerYardVariance> {
        val matched = filter(items) { candidate: SeasonalTrailerYardVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardVariances(store: SeasonalTrailerYardVarianceStore, query: SeasonalTrailerYardVarianceQuery): MutableList<SeasonalTrailerYardVariance> {
    return query.apply(store.list())
}
