package com.acme.transport.network.node.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback node estimates by code. */
interface FallbackNodeEstimateStore {
    fun find(code: String): FallbackNodeEstimate?
    fun save(item: FallbackNodeEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackNodeEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackNodeEstimateStore(
    var items: MutableMap<String, FallbackNodeEstimate>,
    var order: MutableList<String>,
) : FallbackNodeEstimateStore {
    override fun find(code: String): FallbackNodeEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackNodeEstimate) {
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

    override fun list(): MutableList<FallbackNodeEstimate> {
        val out = mutableListOf<FallbackNodeEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackNodeEstimateStore(): FallbackNodeEstimateStore {
    return MemoryFallbackNodeEstimateStore(
        mutableMapOf<String, FallbackNodeEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackNodeEstimateStore(
    var inner: FallbackNodeEstimateStore,
    var cache: TtlCache<FallbackNodeEstimate>,
    var hits: Int,
    var misses: Int,
) : FallbackNodeEstimateStore {
    override fun find(code: String): FallbackNodeEstimate? {
        val hit = (cache.get(code) as FallbackNodeEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackNodeEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackNodeEstimate> = inner.list()
}

fun newCachedFallbackNodeEstimateStore(inner: FallbackNodeEstimateStore, ttl: Int, clock: Clock): FallbackNodeEstimateStore {
    return CachedFallbackNodeEstimateStore(
        inner = inner,
        cache = newTtlCache<FallbackNodeEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback node estimates. Empty fields match all. */
data class FallbackNodeEstimateQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackNodeEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackNodeEstimate>): MutableList<FallbackNodeEstimate> {
        val matched = filter(items) { candidate: FallbackNodeEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackNodeEstimates(store: FallbackNodeEstimateStore, query: FallbackNodeEstimateQuery): MutableList<FallbackNodeEstimate> {
    return query.apply(store.list())
}
