package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient route offset exceptions by code. */
interface LenientRouteOffsetExceptionStore {
    fun find(code: String): LenientRouteOffsetException?
    fun save(item: LenientRouteOffsetException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRouteOffsetException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRouteOffsetExceptionStore(
    var items: MutableMap<String, LenientRouteOffsetException>,
    var order: MutableList<String>,
) : LenientRouteOffsetExceptionStore {
    override fun find(code: String): LenientRouteOffsetException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRouteOffsetException) {
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

    override fun list(): MutableList<LenientRouteOffsetException> {
        val out = mutableListOf<LenientRouteOffsetException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRouteOffsetExceptionStore(): LenientRouteOffsetExceptionStore {
    return MemoryLenientRouteOffsetExceptionStore(
        mutableMapOf<String, LenientRouteOffsetException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRouteOffsetExceptionStore(
    var inner: LenientRouteOffsetExceptionStore,
    var cache: TtlCache<LenientRouteOffsetException>,
    var hits: Int,
    var misses: Int,
) : LenientRouteOffsetExceptionStore {
    override fun find(code: String): LenientRouteOffsetException? {
        val hit = (cache.get(code) as LenientRouteOffsetException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRouteOffsetException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRouteOffsetException> = inner.list()
}

fun newCachedLenientRouteOffsetExceptionStore(inner: LenientRouteOffsetExceptionStore, ttl: Int, clock: Clock): LenientRouteOffsetExceptionStore {
    return CachedLenientRouteOffsetExceptionStore(
        inner = inner,
        cache = newTtlCache<LenientRouteOffsetException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient route offset exceptions. Empty fields match all. */
data class LenientRouteOffsetExceptionQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRouteOffsetException): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRouteOffsetException>): MutableList<LenientRouteOffsetException> {
        val matched = filter(items) { candidate: LenientRouteOffsetException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRouteOffsetExceptions(store: LenientRouteOffsetExceptionStore, query: LenientRouteOffsetExceptionQuery): MutableList<LenientRouteOffsetException> {
    return query.apply(store.list())
}
