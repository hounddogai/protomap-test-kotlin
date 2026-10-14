package com.acme.transport.customs.hold.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered hold variances by code. */
interface TieredHoldVarianceStore {
    fun find(code: String): TieredHoldVariance?
    fun save(item: TieredHoldVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredHoldVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredHoldVarianceStore(
    var items: MutableMap<String, TieredHoldVariance>,
    var order: MutableList<String>,
) : TieredHoldVarianceStore {
    override fun find(code: String): TieredHoldVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredHoldVariance) {
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

    override fun list(): MutableList<TieredHoldVariance> {
        val out = mutableListOf<TieredHoldVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredHoldVarianceStore(): TieredHoldVarianceStore {
    return MemoryTieredHoldVarianceStore(mutableMapOf<String, TieredHoldVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredHoldVarianceStore(
    var inner: TieredHoldVarianceStore,
    var cache: TtlCache<TieredHoldVariance>,
    var hits: Int,
    var misses: Int,
) : TieredHoldVarianceStore {
    override fun find(code: String): TieredHoldVariance? {
        val hit = (cache.get(code) as TieredHoldVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredHoldVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredHoldVariance> = inner.list()
}

fun newCachedTieredHoldVarianceStore(inner: TieredHoldVarianceStore, ttl: Int, clock: Clock): TieredHoldVarianceStore {
    return CachedTieredHoldVarianceStore(
        inner = inner,
        cache = newTtlCache<TieredHoldVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered hold variances. Empty fields match all. */
data class TieredHoldVarianceQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredHoldVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredHoldVariance>): MutableList<TieredHoldVariance> {
        val matched = filter(items) { candidate: TieredHoldVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredHoldVariances(store: TieredHoldVarianceStore, query: TieredHoldVarianceQuery): MutableList<TieredHoldVariance> {
    return query.apply(store.list())
}
