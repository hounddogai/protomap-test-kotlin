package com.acme.transport.emissions.idling.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered idling variances by code. */
interface TieredIdlingVarianceStore {
    fun find(code: String): TieredIdlingVariance?
    fun save(item: TieredIdlingVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIdlingVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIdlingVarianceStore(
    var items: MutableMap<String, TieredIdlingVariance>,
    var order: MutableList<String>,
) : TieredIdlingVarianceStore {
    override fun find(code: String): TieredIdlingVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIdlingVariance) {
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

    override fun list(): MutableList<TieredIdlingVariance> {
        val out = mutableListOf<TieredIdlingVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIdlingVarianceStore(): TieredIdlingVarianceStore {
    return MemoryTieredIdlingVarianceStore(
        mutableMapOf<String, TieredIdlingVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIdlingVarianceStore(
    var inner: TieredIdlingVarianceStore,
    var cache: TtlCache<TieredIdlingVariance>,
    var hits: Int,
    var misses: Int,
) : TieredIdlingVarianceStore {
    override fun find(code: String): TieredIdlingVariance? {
        val hit = (cache.get(code) as TieredIdlingVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIdlingVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIdlingVariance> = inner.list()
}

fun newCachedTieredIdlingVarianceStore(inner: TieredIdlingVarianceStore, ttl: Int, clock: Clock): TieredIdlingVarianceStore {
    return CachedTieredIdlingVarianceStore(
        inner = inner,
        cache = newTtlCache<TieredIdlingVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered idling variances. Empty fields match all. */
data class TieredIdlingVarianceQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIdlingVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredIdlingVariance>): MutableList<TieredIdlingVariance> {
        val matched = filter(items) { candidate: TieredIdlingVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIdlingVariances(store: TieredIdlingVarianceStore, query: TieredIdlingVarianceQuery): MutableList<TieredIdlingVariance> {
    return query.apply(store.list())
}
