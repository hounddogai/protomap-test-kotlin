package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient redelivery estimates by code. */
interface LenientRedeliveryEstimateStore {
    fun find(code: String): LenientRedeliveryEstimate?
    fun save(item: LenientRedeliveryEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRedeliveryEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRedeliveryEstimateStore(
    var items: MutableMap<String, LenientRedeliveryEstimate>,
    var order: MutableList<String>,
) : LenientRedeliveryEstimateStore {
    override fun find(code: String): LenientRedeliveryEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRedeliveryEstimate) {
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

    override fun list(): MutableList<LenientRedeliveryEstimate> {
        val out = mutableListOf<LenientRedeliveryEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRedeliveryEstimateStore(): LenientRedeliveryEstimateStore {
    return MemoryLenientRedeliveryEstimateStore(
        mutableMapOf<String, LenientRedeliveryEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRedeliveryEstimateStore(
    var inner: LenientRedeliveryEstimateStore,
    var cache: TtlCache<LenientRedeliveryEstimate>,
    var hits: Int,
    var misses: Int,
) : LenientRedeliveryEstimateStore {
    override fun find(code: String): LenientRedeliveryEstimate? {
        val hit = (cache.get(code) as LenientRedeliveryEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRedeliveryEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRedeliveryEstimate> = inner.list()
}

fun newCachedLenientRedeliveryEstimateStore(inner: LenientRedeliveryEstimateStore, ttl: Int, clock: Clock): LenientRedeliveryEstimateStore {
    return CachedLenientRedeliveryEstimateStore(
        inner = inner,
        cache = newTtlCache<LenientRedeliveryEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient redelivery estimates. Empty fields match all. */
data class LenientRedeliveryEstimateQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRedeliveryEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LenientRedeliveryEstimate>): MutableList<LenientRedeliveryEstimate> {
        val matched = filter(items) { candidate: LenientRedeliveryEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRedeliveryEstimates(store: LenientRedeliveryEstimateStore, query: LenientRedeliveryEstimateQuery): MutableList<LenientRedeliveryEstimate> {
    return query.apply(store.list())
}
