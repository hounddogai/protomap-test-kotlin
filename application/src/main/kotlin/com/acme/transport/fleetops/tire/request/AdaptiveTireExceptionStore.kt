package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive tire exceptions by code. */
interface AdaptiveTireExceptionStore {
    fun find(code: String): AdaptiveTireException?
    fun save(item: AdaptiveTireException)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTireException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTireExceptionStore(
    var items: MutableMap<String, AdaptiveTireException>,
    var order: MutableList<String>,
) : AdaptiveTireExceptionStore {
    override fun find(code: String): AdaptiveTireException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTireException) {
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

    override fun list(): MutableList<AdaptiveTireException> {
        val out = mutableListOf<AdaptiveTireException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTireExceptionStore(): AdaptiveTireExceptionStore {
    return MemoryAdaptiveTireExceptionStore(
        mutableMapOf<String, AdaptiveTireException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTireExceptionStore(
    var inner: AdaptiveTireExceptionStore,
    var cache: TtlCache<AdaptiveTireException>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTireExceptionStore {
    override fun find(code: String): AdaptiveTireException? {
        val hit = (cache.get(code) as AdaptiveTireException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTireException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTireException> = inner.list()
}

fun newCachedAdaptiveTireExceptionStore(inner: AdaptiveTireExceptionStore, ttl: Int, clock: Clock): AdaptiveTireExceptionStore {
    return CachedAdaptiveTireExceptionStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTireException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tire exceptions. Empty fields match all. */
data class AdaptiveTireExceptionQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTireException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTireException>): MutableList<AdaptiveTireException> {
        val matched = filter(items) { candidate: AdaptiveTireException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTireExceptions(store: AdaptiveTireExceptionStore, query: AdaptiveTireExceptionQuery): MutableList<AdaptiveTireException> {
    return query.apply(store.list())
}
