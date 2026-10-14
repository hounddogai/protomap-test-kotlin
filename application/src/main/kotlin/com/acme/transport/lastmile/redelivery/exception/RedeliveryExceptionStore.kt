package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery exceptions by code. */
interface RedeliveryExceptionStore {
    fun find(code: String): RedeliveryException?
    fun save(item: RedeliveryException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryExceptionStore(
    var items: MutableMap<String, RedeliveryException>,
    var order: MutableList<String>,
) : RedeliveryExceptionStore {
    override fun find(code: String): RedeliveryException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryException) {
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

    override fun list(): MutableList<RedeliveryException> {
        val out = mutableListOf<RedeliveryException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryExceptionStore(): RedeliveryExceptionStore {
    return MemoryRedeliveryExceptionStore(mutableMapOf<String, RedeliveryException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryExceptionStore(
    var inner: RedeliveryExceptionStore,
    var cache: TtlCache<RedeliveryException>,
    var hits: Int,
    var misses: Int,
) : RedeliveryExceptionStore {
    override fun find(code: String): RedeliveryException? {
        val hit = (cache.get(code) as RedeliveryException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryException> = inner.list()
}

fun newCachedRedeliveryExceptionStore(inner: RedeliveryExceptionStore, ttl: Int, clock: Clock): RedeliveryExceptionStore {
    return CachedRedeliveryExceptionStore(
        inner = inner,
        cache = newTtlCache<RedeliveryException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery exceptions. Empty fields match all. */
data class RedeliveryExceptionQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RedeliveryException>): MutableList<RedeliveryException> {
        val matched = filter(items) { candidate: RedeliveryException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryExceptions(store: RedeliveryExceptionStore, query: RedeliveryExceptionQuery): MutableList<RedeliveryException> {
    return query.apply(store.list())
}
