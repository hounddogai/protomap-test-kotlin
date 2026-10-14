package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy hold exceptions by code. */
interface LegacyHoldExceptionStore {
    fun find(code: String): LegacyHoldException?
    fun save(item: LegacyHoldException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyHoldException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyHoldExceptionStore(
    var items: MutableMap<String, LegacyHoldException>,
    var order: MutableList<String>,
) : LegacyHoldExceptionStore {
    override fun find(code: String): LegacyHoldException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyHoldException) {
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

    override fun list(): MutableList<LegacyHoldException> {
        val out = mutableListOf<LegacyHoldException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyHoldExceptionStore(): LegacyHoldExceptionStore {
    return MemoryLegacyHoldExceptionStore(mutableMapOf<String, LegacyHoldException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyHoldExceptionStore(
    var inner: LegacyHoldExceptionStore,
    var cache: TtlCache<LegacyHoldException>,
    var hits: Int,
    var misses: Int,
) : LegacyHoldExceptionStore {
    override fun find(code: String): LegacyHoldException? {
        val hit = (cache.get(code) as LegacyHoldException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyHoldException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyHoldException> = inner.list()
}

fun newCachedLegacyHoldExceptionStore(inner: LegacyHoldExceptionStore, ttl: Int, clock: Clock): LegacyHoldExceptionStore {
    return CachedLegacyHoldExceptionStore(
        inner = inner,
        cache = newTtlCache<LegacyHoldException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy hold exceptions. Empty fields match all. */
data class LegacyHoldExceptionQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyHoldException): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LegacyHoldException>): MutableList<LegacyHoldException> {
        val matched = filter(items) { candidate: LegacyHoldException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyHoldExceptions(store: LegacyHoldExceptionStore, query: LegacyHoldExceptionQuery): MutableList<LegacyHoldException> {
    return query.apply(store.list())
}
