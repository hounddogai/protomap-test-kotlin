package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard estimates by code. */
interface TrailerYardEstimateStore {
    fun find(code: String): TrailerYardEstimate?
    fun save(item: TrailerYardEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardEstimateStore(
    var items: MutableMap<String, TrailerYardEstimate>,
    var order: MutableList<String>,
) : TrailerYardEstimateStore {
    override fun find(code: String): TrailerYardEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardEstimate) {
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

    override fun list(): MutableList<TrailerYardEstimate> {
        val out = mutableListOf<TrailerYardEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardEstimateStore(): TrailerYardEstimateStore {
    return MemoryTrailerYardEstimateStore(mutableMapOf<String, TrailerYardEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardEstimateStore(
    var inner: TrailerYardEstimateStore,
    var cache: TtlCache<TrailerYardEstimate>,
    var hits: Int,
    var misses: Int,
) : TrailerYardEstimateStore {
    override fun find(code: String): TrailerYardEstimate? {
        val hit = (cache.get(code) as TrailerYardEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardEstimate> = inner.list()
}

fun newCachedTrailerYardEstimateStore(inner: TrailerYardEstimateStore, ttl: Int, clock: Clock): TrailerYardEstimateStore {
    return CachedTrailerYardEstimateStore(
        inner = inner,
        cache = newTtlCache<TrailerYardEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard estimates. Empty fields match all. */
data class TrailerYardEstimateQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardEstimate>): MutableList<TrailerYardEstimate> {
        val matched = filter(items) { candidate: TrailerYardEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardEstimates(store: TrailerYardEstimateStore, query: TrailerYardEstimateQuery): MutableList<TrailerYardEstimate> {
    return query.apply(store.list())
}
