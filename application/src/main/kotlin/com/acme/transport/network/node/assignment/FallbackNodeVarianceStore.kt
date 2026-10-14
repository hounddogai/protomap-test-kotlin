package com.acme.transport.network.node.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback node variances by code. */
interface FallbackNodeVarianceStore {
    fun find(code: String): FallbackNodeVariance?
    fun save(item: FallbackNodeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackNodeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackNodeVarianceStore(
    var items: MutableMap<String, FallbackNodeVariance>,
    var order: MutableList<String>,
) : FallbackNodeVarianceStore {
    override fun find(code: String): FallbackNodeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackNodeVariance) {
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

    override fun list(): MutableList<FallbackNodeVariance> {
        val out = mutableListOf<FallbackNodeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackNodeVarianceStore(): FallbackNodeVarianceStore {
    return MemoryFallbackNodeVarianceStore(
        mutableMapOf<String, FallbackNodeVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackNodeVarianceStore(
    var inner: FallbackNodeVarianceStore,
    var cache: TtlCache<FallbackNodeVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackNodeVarianceStore {
    override fun find(code: String): FallbackNodeVariance? {
        val hit = (cache.get(code) as FallbackNodeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackNodeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackNodeVariance> = inner.list()
}

fun newCachedFallbackNodeVarianceStore(inner: FallbackNodeVarianceStore, ttl: Int, clock: Clock): FallbackNodeVarianceStore {
    return CachedFallbackNodeVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackNodeVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback node variances. Empty fields match all. */
data class FallbackNodeVarianceQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackNodeVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackNodeVariance>): MutableList<FallbackNodeVariance> {
        val matched = filter(items) { candidate: FallbackNodeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackNodeVariances(store: FallbackNodeVarianceStore, query: FallbackNodeVarianceQuery): MutableList<FallbackNodeVariance> {
    return query.apply(store.list())
}
