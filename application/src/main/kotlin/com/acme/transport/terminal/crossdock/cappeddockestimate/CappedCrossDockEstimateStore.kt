package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped cross dock estimates by code. */
interface CappedCrossDockEstimateStore {
    fun find(code: String): CappedCrossDockEstimate?
    fun save(item: CappedCrossDockEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCrossDockEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCrossDockEstimateStore(
    var items: MutableMap<String, CappedCrossDockEstimate>,
    var order: MutableList<String>,
) : CappedCrossDockEstimateStore {
    override fun find(code: String): CappedCrossDockEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCrossDockEstimate) {
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

    override fun list(): MutableList<CappedCrossDockEstimate> {
        val out = mutableListOf<CappedCrossDockEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCrossDockEstimateStore(): CappedCrossDockEstimateStore {
    return MemoryCappedCrossDockEstimateStore(
        mutableMapOf<String, CappedCrossDockEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCrossDockEstimateStore(
    var inner: CappedCrossDockEstimateStore,
    var cache: TtlCache<CappedCrossDockEstimate>,
    var hits: Int,
    var misses: Int,
) : CappedCrossDockEstimateStore {
    override fun find(code: String): CappedCrossDockEstimate? {
        val hit = (cache.get(code) as CappedCrossDockEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCrossDockEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCrossDockEstimate> = inner.list()
}

fun newCachedCappedCrossDockEstimateStore(inner: CappedCrossDockEstimateStore, ttl: Int, clock: Clock): CappedCrossDockEstimateStore {
    return CachedCappedCrossDockEstimateStore(
        inner = inner,
        cache = newTtlCache<CappedCrossDockEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped cross dock estimates. Empty fields match all. */
data class CappedCrossDockEstimateQuery(
    var source: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCrossDockEstimate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCrossDockEstimate>): MutableList<CappedCrossDockEstimate> {
        val matched = filter(items) { candidate: CappedCrossDockEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCrossDockEstimates(store: CappedCrossDockEstimateStore, query: CappedCrossDockEstimateQuery): MutableList<CappedCrossDockEstimate> {
    return query.apply(store.list())
}
