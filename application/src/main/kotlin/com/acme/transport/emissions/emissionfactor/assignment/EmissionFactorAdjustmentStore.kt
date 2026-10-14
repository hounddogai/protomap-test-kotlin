package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor adjustments by code. */
interface EmissionFactorAdjustmentStore {
    fun find(code: String): EmissionFactorAdjustment?
    fun save(item: EmissionFactorAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorAdjustmentStore(
    var items: MutableMap<String, EmissionFactorAdjustment>,
    var order: MutableList<String>,
) : EmissionFactorAdjustmentStore {
    override fun find(code: String): EmissionFactorAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorAdjustment) {
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

    override fun list(): MutableList<EmissionFactorAdjustment> {
        val out = mutableListOf<EmissionFactorAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorAdjustmentStore(): EmissionFactorAdjustmentStore {
    return MemoryEmissionFactorAdjustmentStore(
        mutableMapOf<String, EmissionFactorAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorAdjustmentStore(
    var inner: EmissionFactorAdjustmentStore,
    var cache: TtlCache<EmissionFactorAdjustment>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorAdjustmentStore {
    override fun find(code: String): EmissionFactorAdjustment? {
        val hit = (cache.get(code) as EmissionFactorAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorAdjustment> = inner.list()
}

fun newCachedEmissionFactorAdjustmentStore(inner: EmissionFactorAdjustmentStore, ttl: Int, clock: Clock): EmissionFactorAdjustmentStore {
    return CachedEmissionFactorAdjustmentStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor adjustments. Empty fields match all. */
data class EmissionFactorAdjustmentQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorAdjustment>): MutableList<EmissionFactorAdjustment> {
        val matched = filter(items) { candidate: EmissionFactorAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorAdjustments(store: EmissionFactorAdjustmentStore, query: EmissionFactorAdjustmentQuery): MutableList<EmissionFactorAdjustment> {
    return query.apply(store.list())
}
