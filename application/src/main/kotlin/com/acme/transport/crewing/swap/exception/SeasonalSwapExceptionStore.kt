package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal swap exceptions by code. */
interface SeasonalSwapExceptionStore {
    fun find(code: String): SeasonalSwapException?
    fun save(item: SeasonalSwapException)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalSwapException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalSwapExceptionStore(
    var items: MutableMap<String, SeasonalSwapException>,
    var order: MutableList<String>,
) : SeasonalSwapExceptionStore {
    override fun find(code: String): SeasonalSwapException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalSwapException) {
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

    override fun list(): MutableList<SeasonalSwapException> {
        val out = mutableListOf<SeasonalSwapException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalSwapExceptionStore(): SeasonalSwapExceptionStore {
    return MemorySeasonalSwapExceptionStore(
        mutableMapOf<String, SeasonalSwapException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalSwapExceptionStore(
    var inner: SeasonalSwapExceptionStore,
    var cache: TtlCache<SeasonalSwapException>,
    var hits: Int,
    var misses: Int,
) : SeasonalSwapExceptionStore {
    override fun find(code: String): SeasonalSwapException? {
        val hit = (cache.get(code) as SeasonalSwapException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalSwapException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalSwapException> = inner.list()
}

fun newCachedSeasonalSwapExceptionStore(inner: SeasonalSwapExceptionStore, ttl: Int, clock: Clock): SeasonalSwapExceptionStore {
    return CachedSeasonalSwapExceptionStore(
        inner = inner,
        cache = newTtlCache<SeasonalSwapException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal swap exceptions. Empty fields match all. */
data class SeasonalSwapExceptionQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalSwapException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalSwapException>): MutableList<SeasonalSwapException> {
        val matched = filter(items) { candidate: SeasonalSwapException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalSwapExceptions(store: SeasonalSwapExceptionStore, query: SeasonalSwapExceptionQuery): MutableList<SeasonalSwapException> {
    return query.apply(store.list())
}
