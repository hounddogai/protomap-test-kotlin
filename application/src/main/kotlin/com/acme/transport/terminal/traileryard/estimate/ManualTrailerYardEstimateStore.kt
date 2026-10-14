package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual trailer yard estimates by code. */
interface ManualTrailerYardEstimateStore {
    fun find(code: String): ManualTrailerYardEstimate?
    fun save(item: ManualTrailerYardEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerYardEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerYardEstimateStore(
    var items: MutableMap<String, ManualTrailerYardEstimate>,
    var order: MutableList<String>,
) : ManualTrailerYardEstimateStore {
    override fun find(code: String): ManualTrailerYardEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerYardEstimate) {
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

    override fun list(): MutableList<ManualTrailerYardEstimate> {
        val out = mutableListOf<ManualTrailerYardEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerYardEstimateStore(): ManualTrailerYardEstimateStore {
    return MemoryManualTrailerYardEstimateStore(
        mutableMapOf<String, ManualTrailerYardEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerYardEstimateStore(
    var inner: ManualTrailerYardEstimateStore,
    var cache: TtlCache<ManualTrailerYardEstimate>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerYardEstimateStore {
    override fun find(code: String): ManualTrailerYardEstimate? {
        val hit = (cache.get(code) as ManualTrailerYardEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerYardEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerYardEstimate> = inner.list()
}

fun newCachedManualTrailerYardEstimateStore(inner: ManualTrailerYardEstimateStore, ttl: Int, clock: Clock): ManualTrailerYardEstimateStore {
    return CachedManualTrailerYardEstimateStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerYardEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer yard estimates. Empty fields match all. */
data class ManualTrailerYardEstimateQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerYardEstimate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTrailerYardEstimate>): MutableList<ManualTrailerYardEstimate> {
        val matched = filter(items) { candidate: ManualTrailerYardEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerYardEstimates(store: ManualTrailerYardEstimateStore, query: ManualTrailerYardEstimateQuery): MutableList<ManualTrailerYardEstimate> {
    return query.apply(store.list())
}
