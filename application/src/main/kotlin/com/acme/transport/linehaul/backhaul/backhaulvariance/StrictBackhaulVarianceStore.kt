package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict backhaul variances by code. */
interface StrictBackhaulVarianceStore {
    fun find(code: String): StrictBackhaulVariance?
    fun save(item: StrictBackhaulVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBackhaulVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBackhaulVarianceStore(
    var items: MutableMap<String, StrictBackhaulVariance>,
    var order: MutableList<String>,
) : StrictBackhaulVarianceStore {
    override fun find(code: String): StrictBackhaulVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBackhaulVariance) {
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

    override fun list(): MutableList<StrictBackhaulVariance> {
        val out = mutableListOf<StrictBackhaulVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBackhaulVarianceStore(): StrictBackhaulVarianceStore {
    return MemoryStrictBackhaulVarianceStore(
        mutableMapOf<String, StrictBackhaulVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBackhaulVarianceStore(
    var inner: StrictBackhaulVarianceStore,
    var cache: TtlCache<StrictBackhaulVariance>,
    var hits: Int,
    var misses: Int,
) : StrictBackhaulVarianceStore {
    override fun find(code: String): StrictBackhaulVariance? {
        val hit = (cache.get(code) as StrictBackhaulVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBackhaulVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBackhaulVariance> = inner.list()
}

fun newCachedStrictBackhaulVarianceStore(inner: StrictBackhaulVarianceStore, ttl: Int, clock: Clock): StrictBackhaulVarianceStore {
    return CachedStrictBackhaulVarianceStore(
        inner = inner,
        cache = newTtlCache<StrictBackhaulVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict backhaul variances. Empty fields match all. */
data class StrictBackhaulVarianceQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBackhaulVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBackhaulVariance>): MutableList<StrictBackhaulVariance> {
        val matched = filter(items) { candidate: StrictBackhaulVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBackhaulVariances(store: StrictBackhaulVarianceStore, query: StrictBackhaulVarianceQuery): MutableList<StrictBackhaulVariance> {
    return query.apply(store.list())
}
