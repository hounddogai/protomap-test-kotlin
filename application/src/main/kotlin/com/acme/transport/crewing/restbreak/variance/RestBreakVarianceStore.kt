package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break variances by code. */
interface RestBreakVarianceStore {
    fun find(code: String): RestBreakVariance?
    fun save(item: RestBreakVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakVarianceStore(
    var items: MutableMap<String, RestBreakVariance>,
    var order: MutableList<String>,
) : RestBreakVarianceStore {
    override fun find(code: String): RestBreakVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakVariance) {
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

    override fun list(): MutableList<RestBreakVariance> {
        val out = mutableListOf<RestBreakVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakVarianceStore(): RestBreakVarianceStore {
    return MemoryRestBreakVarianceStore(mutableMapOf<String, RestBreakVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakVarianceStore(
    var inner: RestBreakVarianceStore,
    var cache: TtlCache<RestBreakVariance>,
    var hits: Int,
    var misses: Int,
) : RestBreakVarianceStore {
    override fun find(code: String): RestBreakVariance? {
        val hit = (cache.get(code) as RestBreakVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakVariance> = inner.list()
}

fun newCachedRestBreakVarianceStore(inner: RestBreakVarianceStore, ttl: Int, clock: Clock): RestBreakVarianceStore {
    return CachedRestBreakVarianceStore(
        inner = inner,
        cache = newTtlCache<RestBreakVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break variances. Empty fields match all. */
data class RestBreakVarianceQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RestBreakVariance>): MutableList<RestBreakVariance> {
        val matched = filter(items) { candidate: RestBreakVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakVariances(store: RestBreakVarianceStore, query: RestBreakVarianceQuery): MutableList<RestBreakVariance> {
    return query.apply(store.list())
}
