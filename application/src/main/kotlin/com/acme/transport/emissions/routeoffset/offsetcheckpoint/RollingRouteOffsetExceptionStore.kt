package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling route offset exceptions by code. */
interface RollingRouteOffsetExceptionStore {
    fun find(code: String): RollingRouteOffsetException?
    fun save(item: RollingRouteOffsetException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRouteOffsetException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRouteOffsetExceptionStore(
    var items: MutableMap<String, RollingRouteOffsetException>,
    var order: MutableList<String>,
) : RollingRouteOffsetExceptionStore {
    override fun find(code: String): RollingRouteOffsetException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRouteOffsetException) {
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

    override fun list(): MutableList<RollingRouteOffsetException> {
        val out = mutableListOf<RollingRouteOffsetException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRouteOffsetExceptionStore(): RollingRouteOffsetExceptionStore {
    return MemoryRollingRouteOffsetExceptionStore(
        mutableMapOf<String, RollingRouteOffsetException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRouteOffsetExceptionStore(
    var inner: RollingRouteOffsetExceptionStore,
    var cache: TtlCache<RollingRouteOffsetException>,
    var hits: Int,
    var misses: Int,
) : RollingRouteOffsetExceptionStore {
    override fun find(code: String): RollingRouteOffsetException? {
        val hit = (cache.get(code) as RollingRouteOffsetException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRouteOffsetException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRouteOffsetException> = inner.list()
}

fun newCachedRollingRouteOffsetExceptionStore(inner: RollingRouteOffsetExceptionStore, ttl: Int, clock: Clock): RollingRouteOffsetExceptionStore {
    return CachedRollingRouteOffsetExceptionStore(
        inner = inner,
        cache = newTtlCache<RollingRouteOffsetException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling route offset exceptions. Empty fields match all. */
data class RollingRouteOffsetExceptionQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRouteOffsetException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRouteOffsetException>): MutableList<RollingRouteOffsetException> {
        val matched = filter(items) { candidate: RollingRouteOffsetException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRouteOffsetExceptions(store: RollingRouteOffsetExceptionStore, query: RollingRouteOffsetExceptionQuery): MutableList<RollingRouteOffsetException> {
    return query.apply(store.list())
}
