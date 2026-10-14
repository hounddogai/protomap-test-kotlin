package com.acme.transport.customs.hold.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered hold estimates by code. */
interface TieredHoldEstimateStore {
    fun find(code: String): TieredHoldEstimate?
    fun save(item: TieredHoldEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredHoldEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredHoldEstimateStore(
    var items: MutableMap<String, TieredHoldEstimate>,
    var order: MutableList<String>,
) : TieredHoldEstimateStore {
    override fun find(code: String): TieredHoldEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredHoldEstimate) {
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

    override fun list(): MutableList<TieredHoldEstimate> {
        val out = mutableListOf<TieredHoldEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredHoldEstimateStore(): TieredHoldEstimateStore {
    return MemoryTieredHoldEstimateStore(mutableMapOf<String, TieredHoldEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredHoldEstimateStore(
    var inner: TieredHoldEstimateStore,
    var cache: TtlCache<TieredHoldEstimate>,
    var hits: Int,
    var misses: Int,
) : TieredHoldEstimateStore {
    override fun find(code: String): TieredHoldEstimate? {
        val hit = (cache.get(code) as TieredHoldEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredHoldEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredHoldEstimate> = inner.list()
}

fun newCachedTieredHoldEstimateStore(inner: TieredHoldEstimateStore, ttl: Int, clock: Clock): TieredHoldEstimateStore {
    return CachedTieredHoldEstimateStore(
        inner = inner,
        cache = newTtlCache<TieredHoldEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered hold estimates. Empty fields match all. */
data class TieredHoldEstimateQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredHoldEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredHoldEstimate>): MutableList<TieredHoldEstimate> {
        val matched = filter(items) { candidate: TieredHoldEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredHoldEstimates(store: TieredHoldEstimateStore, query: TieredHoldEstimateQuery): MutableList<TieredHoldEstimate> {
    return query.apply(store.list())
}
