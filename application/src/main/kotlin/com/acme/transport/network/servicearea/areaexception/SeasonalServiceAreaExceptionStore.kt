package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal service area exceptions by code. */
interface SeasonalServiceAreaExceptionStore {
    fun find(code: String): SeasonalServiceAreaException?
    fun save(item: SeasonalServiceAreaException)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServiceAreaException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServiceAreaExceptionStore(
    var items: MutableMap<String, SeasonalServiceAreaException>,
    var order: MutableList<String>,
) : SeasonalServiceAreaExceptionStore {
    override fun find(code: String): SeasonalServiceAreaException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServiceAreaException) {
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

    override fun list(): MutableList<SeasonalServiceAreaException> {
        val out = mutableListOf<SeasonalServiceAreaException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServiceAreaExceptionStore(): SeasonalServiceAreaExceptionStore {
    return MemorySeasonalServiceAreaExceptionStore(
        mutableMapOf<String, SeasonalServiceAreaException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServiceAreaExceptionStore(
    var inner: SeasonalServiceAreaExceptionStore,
    var cache: TtlCache<SeasonalServiceAreaException>,
    var hits: Int,
    var misses: Int,
) : SeasonalServiceAreaExceptionStore {
    override fun find(code: String): SeasonalServiceAreaException? {
        val hit = (cache.get(code) as SeasonalServiceAreaException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServiceAreaException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServiceAreaException> = inner.list()
}

fun newCachedSeasonalServiceAreaExceptionStore(inner: SeasonalServiceAreaExceptionStore, ttl: Int, clock: Clock): SeasonalServiceAreaExceptionStore {
    return CachedSeasonalServiceAreaExceptionStore(
        inner = inner,
        cache = newTtlCache<SeasonalServiceAreaException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service area exceptions. Empty fields match all. */
data class SeasonalServiceAreaExceptionQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServiceAreaException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServiceAreaException>): MutableList<SeasonalServiceAreaException> {
        val matched = filter(items) { candidate: SeasonalServiceAreaException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServiceAreaExceptions(store: SeasonalServiceAreaExceptionStore, query: SeasonalServiceAreaExceptionQuery): MutableList<SeasonalServiceAreaException> {
    return query.apply(store.list())
}
