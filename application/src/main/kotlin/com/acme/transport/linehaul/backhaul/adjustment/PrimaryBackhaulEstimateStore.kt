package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary backhaul estimates by code. */
interface PrimaryBackhaulEstimateStore {
    fun find(code: String): PrimaryBackhaulEstimate?
    fun save(item: PrimaryBackhaulEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryBackhaulEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryBackhaulEstimateStore(
    var items: MutableMap<String, PrimaryBackhaulEstimate>,
    var order: MutableList<String>,
) : PrimaryBackhaulEstimateStore {
    override fun find(code: String): PrimaryBackhaulEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryBackhaulEstimate) {
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

    override fun list(): MutableList<PrimaryBackhaulEstimate> {
        val out = mutableListOf<PrimaryBackhaulEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryBackhaulEstimateStore(): PrimaryBackhaulEstimateStore {
    return MemoryPrimaryBackhaulEstimateStore(
        mutableMapOf<String, PrimaryBackhaulEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryBackhaulEstimateStore(
    var inner: PrimaryBackhaulEstimateStore,
    var cache: TtlCache<PrimaryBackhaulEstimate>,
    var hits: Int,
    var misses: Int,
) : PrimaryBackhaulEstimateStore {
    override fun find(code: String): PrimaryBackhaulEstimate? {
        val hit = (cache.get(code) as PrimaryBackhaulEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryBackhaulEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryBackhaulEstimate> = inner.list()
}

fun newCachedPrimaryBackhaulEstimateStore(inner: PrimaryBackhaulEstimateStore, ttl: Int, clock: Clock): PrimaryBackhaulEstimateStore {
    return CachedPrimaryBackhaulEstimateStore(
        inner = inner,
        cache = newTtlCache<PrimaryBackhaulEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary backhaul estimates. Empty fields match all. */
data class PrimaryBackhaulEstimateQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryBackhaulEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryBackhaulEstimate>): MutableList<PrimaryBackhaulEstimate> {
        val matched = filter(items) { candidate: PrimaryBackhaulEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryBackhaulEstimates(store: PrimaryBackhaulEstimateStore, query: PrimaryBackhaulEstimateQuery): MutableList<PrimaryBackhaulEstimate> {
    return query.apply(store.list())
}
