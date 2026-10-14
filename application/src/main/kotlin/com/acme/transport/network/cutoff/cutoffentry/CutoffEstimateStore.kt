package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff estimates by code. */
interface CutoffEstimateStore {
    fun find(code: String): CutoffEstimate?
    fun save(item: CutoffEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffEstimateStore(
    var items: MutableMap<String, CutoffEstimate>,
    var order: MutableList<String>,
) : CutoffEstimateStore {
    override fun find(code: String): CutoffEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffEstimate) {
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

    override fun list(): MutableList<CutoffEstimate> {
        val out = mutableListOf<CutoffEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffEstimateStore(): CutoffEstimateStore {
    return MemoryCutoffEstimateStore(mutableMapOf<String, CutoffEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffEstimateStore(
    var inner: CutoffEstimateStore,
    var cache: TtlCache<CutoffEstimate>,
    var hits: Int,
    var misses: Int,
) : CutoffEstimateStore {
    override fun find(code: String): CutoffEstimate? {
        val hit = (cache.get(code) as CutoffEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffEstimate> = inner.list()
}

fun newCachedCutoffEstimateStore(inner: CutoffEstimateStore, ttl: Int, clock: Clock): CutoffEstimateStore {
    return CachedCutoffEstimateStore(
        inner = inner,
        cache = newTtlCache<CutoffEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff estimates. Empty fields match all. */
data class CutoffEstimateQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffEstimate>): MutableList<CutoffEstimate> {
        val matched = filter(items) { candidate: CutoffEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffEstimates(store: CutoffEstimateStore, query: CutoffEstimateQuery): MutableList<CutoffEstimate> {
    return query.apply(store.list())
}
