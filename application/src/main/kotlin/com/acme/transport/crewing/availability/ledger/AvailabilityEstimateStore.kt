package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability estimates by code. */
interface AvailabilityEstimateStore {
    fun find(code: String): AvailabilityEstimate?
    fun save(item: AvailabilityEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityEstimateStore(
    var items: MutableMap<String, AvailabilityEstimate>,
    var order: MutableList<String>,
) : AvailabilityEstimateStore {
    override fun find(code: String): AvailabilityEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityEstimate) {
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

    override fun list(): MutableList<AvailabilityEstimate> {
        val out = mutableListOf<AvailabilityEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityEstimateStore(): AvailabilityEstimateStore {
    return MemoryAvailabilityEstimateStore(
        mutableMapOf<String, AvailabilityEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityEstimateStore(
    var inner: AvailabilityEstimateStore,
    var cache: TtlCache<AvailabilityEstimate>,
    var hits: Int,
    var misses: Int,
) : AvailabilityEstimateStore {
    override fun find(code: String): AvailabilityEstimate? {
        val hit = (cache.get(code) as AvailabilityEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityEstimate> = inner.list()
}

fun newCachedAvailabilityEstimateStore(inner: AvailabilityEstimateStore, ttl: Int, clock: Clock): AvailabilityEstimateStore {
    return CachedAvailabilityEstimateStore(
        inner = inner,
        cache = newTtlCache<AvailabilityEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability estimates. Empty fields match all. */
data class AvailabilityEstimateQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityEstimate>): MutableList<AvailabilityEstimate> {
        val matched = filter(items) { candidate: AvailabilityEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityEstimates(store: AvailabilityEstimateStore, query: AvailabilityEstimateQuery): MutableList<AvailabilityEstimate> {
    return query.apply(store.list())
}
