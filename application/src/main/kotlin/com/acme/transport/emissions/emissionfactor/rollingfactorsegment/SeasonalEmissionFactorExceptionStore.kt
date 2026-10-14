package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal emission factor exceptions by code. */
interface SeasonalEmissionFactorExceptionStore {
    fun find(code: String): SeasonalEmissionFactorException?
    fun save(item: SeasonalEmissionFactorException)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEmissionFactorException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEmissionFactorExceptionStore(
    var items: MutableMap<String, SeasonalEmissionFactorException>,
    var order: MutableList<String>,
) : SeasonalEmissionFactorExceptionStore {
    override fun find(code: String): SeasonalEmissionFactorException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEmissionFactorException) {
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

    override fun list(): MutableList<SeasonalEmissionFactorException> {
        val out = mutableListOf<SeasonalEmissionFactorException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEmissionFactorExceptionStore(): SeasonalEmissionFactorExceptionStore {
    return MemorySeasonalEmissionFactorExceptionStore(
        mutableMapOf<String, SeasonalEmissionFactorException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEmissionFactorExceptionStore(
    var inner: SeasonalEmissionFactorExceptionStore,
    var cache: TtlCache<SeasonalEmissionFactorException>,
    var hits: Int,
    var misses: Int,
) : SeasonalEmissionFactorExceptionStore {
    override fun find(code: String): SeasonalEmissionFactorException? {
        val hit = (cache.get(code) as SeasonalEmissionFactorException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEmissionFactorException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEmissionFactorException> = inner.list()
}

fun newCachedSeasonalEmissionFactorExceptionStore(inner: SeasonalEmissionFactorExceptionStore, ttl: Int, clock: Clock): SeasonalEmissionFactorExceptionStore {
    return CachedSeasonalEmissionFactorExceptionStore(
        inner = inner,
        cache = newTtlCache<SeasonalEmissionFactorException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal emission factor exceptions. Empty fields match all. */
data class SeasonalEmissionFactorExceptionQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEmissionFactorException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalEmissionFactorException>): MutableList<SeasonalEmissionFactorException> {
        val matched = filter(items) { candidate: SeasonalEmissionFactorException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEmissionFactorExceptions(store: SeasonalEmissionFactorExceptionStore, query: SeasonalEmissionFactorExceptionQuery): MutableList<SeasonalEmissionFactorException> {
    return query.apply(store.list())
}
