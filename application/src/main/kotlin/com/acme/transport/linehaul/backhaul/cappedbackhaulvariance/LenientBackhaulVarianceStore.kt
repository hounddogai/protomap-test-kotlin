package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient backhaul variances by code. */
interface LenientBackhaulVarianceStore {
    fun find(code: String): LenientBackhaulVariance?
    fun save(item: LenientBackhaulVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientBackhaulVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientBackhaulVarianceStore(
    var items: MutableMap<String, LenientBackhaulVariance>,
    var order: MutableList<String>,
) : LenientBackhaulVarianceStore {
    override fun find(code: String): LenientBackhaulVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientBackhaulVariance) {
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

    override fun list(): MutableList<LenientBackhaulVariance> {
        val out = mutableListOf<LenientBackhaulVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientBackhaulVarianceStore(): LenientBackhaulVarianceStore {
    return MemoryLenientBackhaulVarianceStore(
        mutableMapOf<String, LenientBackhaulVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientBackhaulVarianceStore(
    var inner: LenientBackhaulVarianceStore,
    var cache: TtlCache<LenientBackhaulVariance>,
    var hits: Int,
    var misses: Int,
) : LenientBackhaulVarianceStore {
    override fun find(code: String): LenientBackhaulVariance? {
        val hit = (cache.get(code) as LenientBackhaulVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientBackhaulVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientBackhaulVariance> = inner.list()
}

fun newCachedLenientBackhaulVarianceStore(inner: LenientBackhaulVarianceStore, ttl: Int, clock: Clock): LenientBackhaulVarianceStore {
    return CachedLenientBackhaulVarianceStore(
        inner = inner,
        cache = newTtlCache<LenientBackhaulVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient backhaul variances. Empty fields match all. */
data class LenientBackhaulVarianceQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientBackhaulVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientBackhaulVariance>): MutableList<LenientBackhaulVariance> {
        val matched = filter(items) { candidate: LenientBackhaulVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientBackhaulVariances(store: LenientBackhaulVarianceStore, query: LenientBackhaulVarianceQuery): MutableList<LenientBackhaulVariance> {
    return query.apply(store.list())
}
