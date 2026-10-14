package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped ev charge variances by code. */
interface CappedEvChargeVarianceStore {
    fun find(code: String): CappedEvChargeVariance?
    fun save(item: CappedEvChargeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedEvChargeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedEvChargeVarianceStore(
    var items: MutableMap<String, CappedEvChargeVariance>,
    var order: MutableList<String>,
) : CappedEvChargeVarianceStore {
    override fun find(code: String): CappedEvChargeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedEvChargeVariance) {
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

    override fun list(): MutableList<CappedEvChargeVariance> {
        val out = mutableListOf<CappedEvChargeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedEvChargeVarianceStore(): CappedEvChargeVarianceStore {
    return MemoryCappedEvChargeVarianceStore(
        mutableMapOf<String, CappedEvChargeVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedEvChargeVarianceStore(
    var inner: CappedEvChargeVarianceStore,
    var cache: TtlCache<CappedEvChargeVariance>,
    var hits: Int,
    var misses: Int,
) : CappedEvChargeVarianceStore {
    override fun find(code: String): CappedEvChargeVariance? {
        val hit = (cache.get(code) as CappedEvChargeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedEvChargeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedEvChargeVariance> = inner.list()
}

fun newCachedCappedEvChargeVarianceStore(inner: CappedEvChargeVarianceStore, ttl: Int, clock: Clock): CappedEvChargeVarianceStore {
    return CachedCappedEvChargeVarianceStore(
        inner = inner,
        cache = newTtlCache<CappedEvChargeVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped ev charge variances. Empty fields match all. */
data class CappedEvChargeVarianceQuery(
    var category: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedEvChargeVariance): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedEvChargeVariance>): MutableList<CappedEvChargeVariance> {
        val matched = filter(items) { candidate: CappedEvChargeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedEvChargeVariances(store: CappedEvChargeVarianceStore, query: CappedEvChargeVarianceQuery): MutableList<CappedEvChargeVariance> {
    return query.apply(store.list())
}
