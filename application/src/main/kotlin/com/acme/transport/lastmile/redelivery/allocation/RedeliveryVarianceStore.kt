package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery variances by code. */
interface RedeliveryVarianceStore {
    fun find(code: String): RedeliveryVariance?
    fun save(item: RedeliveryVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryVarianceStore(
    var items: MutableMap<String, RedeliveryVariance>,
    var order: MutableList<String>,
) : RedeliveryVarianceStore {
    override fun find(code: String): RedeliveryVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryVariance) {
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

    override fun list(): MutableList<RedeliveryVariance> {
        val out = mutableListOf<RedeliveryVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryVarianceStore(): RedeliveryVarianceStore {
    return MemoryRedeliveryVarianceStore(mutableMapOf<String, RedeliveryVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryVarianceStore(
    var inner: RedeliveryVarianceStore,
    var cache: TtlCache<RedeliveryVariance>,
    var hits: Int,
    var misses: Int,
) : RedeliveryVarianceStore {
    override fun find(code: String): RedeliveryVariance? {
        val hit = (cache.get(code) as RedeliveryVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryVariance> = inner.list()
}

fun newCachedRedeliveryVarianceStore(inner: RedeliveryVarianceStore, ttl: Int, clock: Clock): RedeliveryVarianceStore {
    return CachedRedeliveryVarianceStore(
        inner = inner,
        cache = newTtlCache<RedeliveryVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery variances. Empty fields match all. */
data class RedeliveryVarianceQuery(
    var unit: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryVariance>): MutableList<RedeliveryVariance> {
        val matched = filter(items) { candidate: RedeliveryVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryVariances(store: RedeliveryVarianceStore, query: RedeliveryVarianceQuery): MutableList<RedeliveryVariance> {
    return query.apply(store.list())
}
