package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual fuel burn variances by code. */
interface ManualFuelBurnVarianceStore {
    fun find(code: String): ManualFuelBurnVariance?
    fun save(item: ManualFuelBurnVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFuelBurnVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFuelBurnVarianceStore(
    var items: MutableMap<String, ManualFuelBurnVariance>,
    var order: MutableList<String>,
) : ManualFuelBurnVarianceStore {
    override fun find(code: String): ManualFuelBurnVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFuelBurnVariance) {
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

    override fun list(): MutableList<ManualFuelBurnVariance> {
        val out = mutableListOf<ManualFuelBurnVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFuelBurnVarianceStore(): ManualFuelBurnVarianceStore {
    return MemoryManualFuelBurnVarianceStore(
        mutableMapOf<String, ManualFuelBurnVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFuelBurnVarianceStore(
    var inner: ManualFuelBurnVarianceStore,
    var cache: TtlCache<ManualFuelBurnVariance>,
    var hits: Int,
    var misses: Int,
) : ManualFuelBurnVarianceStore {
    override fun find(code: String): ManualFuelBurnVariance? {
        val hit = (cache.get(code) as ManualFuelBurnVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFuelBurnVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFuelBurnVariance> = inner.list()
}

fun newCachedManualFuelBurnVarianceStore(inner: ManualFuelBurnVarianceStore, ttl: Int, clock: Clock): ManualFuelBurnVarianceStore {
    return CachedManualFuelBurnVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualFuelBurnVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual fuel burn variances. Empty fields match all. */
data class ManualFuelBurnVarianceQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFuelBurnVariance): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFuelBurnVariance>): MutableList<ManualFuelBurnVariance> {
        val matched = filter(items) { candidate: ManualFuelBurnVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFuelBurnVariances(store: ManualFuelBurnVarianceStore, query: ManualFuelBurnVarianceQuery): MutableList<ManualFuelBurnVariance> {
    return query.apply(store.list())
}
