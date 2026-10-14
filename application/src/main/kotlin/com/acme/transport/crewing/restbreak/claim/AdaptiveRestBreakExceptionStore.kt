package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive rest break exceptions by code. */
interface AdaptiveRestBreakExceptionStore {
    fun find(code: String): AdaptiveRestBreakException?
    fun save(item: AdaptiveRestBreakException)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRestBreakException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRestBreakExceptionStore(
    var items: MutableMap<String, AdaptiveRestBreakException>,
    var order: MutableList<String>,
) : AdaptiveRestBreakExceptionStore {
    override fun find(code: String): AdaptiveRestBreakException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRestBreakException) {
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

    override fun list(): MutableList<AdaptiveRestBreakException> {
        val out = mutableListOf<AdaptiveRestBreakException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRestBreakExceptionStore(): AdaptiveRestBreakExceptionStore {
    return MemoryAdaptiveRestBreakExceptionStore(
        mutableMapOf<String, AdaptiveRestBreakException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRestBreakExceptionStore(
    var inner: AdaptiveRestBreakExceptionStore,
    var cache: TtlCache<AdaptiveRestBreakException>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRestBreakExceptionStore {
    override fun find(code: String): AdaptiveRestBreakException? {
        val hit = (cache.get(code) as AdaptiveRestBreakException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRestBreakException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRestBreakException> = inner.list()
}

fun newCachedAdaptiveRestBreakExceptionStore(inner: AdaptiveRestBreakExceptionStore, ttl: Int, clock: Clock): AdaptiveRestBreakExceptionStore {
    return CachedAdaptiveRestBreakExceptionStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRestBreakException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive rest break exceptions. Empty fields match all. */
data class AdaptiveRestBreakExceptionQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRestBreakException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRestBreakException>): MutableList<AdaptiveRestBreakException> {
        val matched = filter(items) { candidate: AdaptiveRestBreakException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRestBreakExceptions(store: AdaptiveRestBreakExceptionStore, query: AdaptiveRestBreakExceptionQuery): MutableList<AdaptiveRestBreakException> {
    return query.apply(store.list())
}
