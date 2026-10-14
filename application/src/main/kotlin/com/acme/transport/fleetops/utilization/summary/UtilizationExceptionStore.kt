package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization exceptions by code. */
interface UtilizationExceptionStore {
    fun find(code: String): UtilizationException?
    fun save(item: UtilizationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationExceptionStore(
    var items: MutableMap<String, UtilizationException>,
    var order: MutableList<String>,
) : UtilizationExceptionStore {
    override fun find(code: String): UtilizationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationException) {
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

    override fun list(): MutableList<UtilizationException> {
        val out = mutableListOf<UtilizationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationExceptionStore(): UtilizationExceptionStore {
    return MemoryUtilizationExceptionStore(
        mutableMapOf<String, UtilizationException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationExceptionStore(
    var inner: UtilizationExceptionStore,
    var cache: TtlCache<UtilizationException>,
    var hits: Int,
    var misses: Int,
) : UtilizationExceptionStore {
    override fun find(code: String): UtilizationException? {
        val hit = (cache.get(code) as UtilizationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationException> = inner.list()
}

fun newCachedUtilizationExceptionStore(inner: UtilizationExceptionStore, ttl: Int, clock: Clock): UtilizationExceptionStore {
    return CachedUtilizationExceptionStore(
        inner = inner,
        cache = newTtlCache<UtilizationException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization exceptions. Empty fields match all. */
data class UtilizationExceptionQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationException): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationException>): MutableList<UtilizationException> {
        val matched = filter(items) { candidate: UtilizationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationExceptions(store: UtilizationExceptionStore, query: UtilizationExceptionQuery): MutableList<UtilizationException> {
    return query.apply(store.list())
}
