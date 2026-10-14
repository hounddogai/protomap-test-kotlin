package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback idling variances by code. */
interface FallbackIdlingVarianceStore {
    fun find(code: String): FallbackIdlingVariance?
    fun save(item: FallbackIdlingVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackIdlingVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackIdlingVarianceStore(
    var items: MutableMap<String, FallbackIdlingVariance>,
    var order: MutableList<String>,
) : FallbackIdlingVarianceStore {
    override fun find(code: String): FallbackIdlingVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackIdlingVariance) {
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

    override fun list(): MutableList<FallbackIdlingVariance> {
        val out = mutableListOf<FallbackIdlingVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackIdlingVarianceStore(): FallbackIdlingVarianceStore {
    return MemoryFallbackIdlingVarianceStore(
        mutableMapOf<String, FallbackIdlingVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackIdlingVarianceStore(
    var inner: FallbackIdlingVarianceStore,
    var cache: TtlCache<FallbackIdlingVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackIdlingVarianceStore {
    override fun find(code: String): FallbackIdlingVariance? {
        val hit = (cache.get(code) as FallbackIdlingVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackIdlingVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackIdlingVariance> = inner.list()
}

fun newCachedFallbackIdlingVarianceStore(inner: FallbackIdlingVarianceStore, ttl: Int, clock: Clock): FallbackIdlingVarianceStore {
    return CachedFallbackIdlingVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackIdlingVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback idling variances. Empty fields match all. */
data class FallbackIdlingVarianceQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackIdlingVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackIdlingVariance>): MutableList<FallbackIdlingVariance> {
        val matched = filter(items) { candidate: FallbackIdlingVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackIdlingVariances(store: FallbackIdlingVarianceStore, query: FallbackIdlingVarianceQuery): MutableList<FallbackIdlingVariance> {
    return query.apply(store.list())
}
