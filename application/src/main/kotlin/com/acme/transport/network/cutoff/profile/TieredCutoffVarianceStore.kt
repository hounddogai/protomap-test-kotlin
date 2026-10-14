package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered cutoff variances by code. */
interface TieredCutoffVarianceStore {
    fun find(code: String): TieredCutoffVariance?
    fun save(item: TieredCutoffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCutoffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCutoffVarianceStore(
    var items: MutableMap<String, TieredCutoffVariance>,
    var order: MutableList<String>,
) : TieredCutoffVarianceStore {
    override fun find(code: String): TieredCutoffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCutoffVariance) {
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

    override fun list(): MutableList<TieredCutoffVariance> {
        val out = mutableListOf<TieredCutoffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCutoffVarianceStore(): TieredCutoffVarianceStore {
    return MemoryTieredCutoffVarianceStore(
        mutableMapOf<String, TieredCutoffVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCutoffVarianceStore(
    var inner: TieredCutoffVarianceStore,
    var cache: TtlCache<TieredCutoffVariance>,
    var hits: Int,
    var misses: Int,
) : TieredCutoffVarianceStore {
    override fun find(code: String): TieredCutoffVariance? {
        val hit = (cache.get(code) as TieredCutoffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCutoffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCutoffVariance> = inner.list()
}

fun newCachedTieredCutoffVarianceStore(inner: TieredCutoffVarianceStore, ttl: Int, clock: Clock): TieredCutoffVarianceStore {
    return CachedTieredCutoffVarianceStore(
        inner = inner,
        cache = newTtlCache<TieredCutoffVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cutoff variances. Empty fields match all. */
data class TieredCutoffVarianceQuery(
    var segment: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCutoffVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCutoffVariance>): MutableList<TieredCutoffVariance> {
        val matched = filter(items) { candidate: TieredCutoffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCutoffVariances(store: TieredCutoffVarianceStore, query: TieredCutoffVarianceQuery): MutableList<TieredCutoffVariance> {
    return query.apply(store.list())
}
