package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge adjustments by code. */
interface EvChargeAdjustmentStore {
    fun find(code: String): EvChargeAdjustment?
    fun save(item: EvChargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeAdjustmentStore(
    var items: MutableMap<String, EvChargeAdjustment>,
    var order: MutableList<String>,
) : EvChargeAdjustmentStore {
    override fun find(code: String): EvChargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeAdjustment) {
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

    override fun list(): MutableList<EvChargeAdjustment> {
        val out = mutableListOf<EvChargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeAdjustmentStore(): EvChargeAdjustmentStore {
    return MemoryEvChargeAdjustmentStore(mutableMapOf<String, EvChargeAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeAdjustmentStore(
    var inner: EvChargeAdjustmentStore,
    var cache: TtlCache<EvChargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : EvChargeAdjustmentStore {
    override fun find(code: String): EvChargeAdjustment? {
        val hit = (cache.get(code) as EvChargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeAdjustment> = inner.list()
}

fun newCachedEvChargeAdjustmentStore(inner: EvChargeAdjustmentStore, ttl: Int, clock: Clock): EvChargeAdjustmentStore {
    return CachedEvChargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<EvChargeAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge adjustments. Empty fields match all. */
data class EvChargeAdjustmentQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<EvChargeAdjustment>): MutableList<EvChargeAdjustment> {
        val matched = filter(items) { candidate: EvChargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeAdjustments(store: EvChargeAdjustmentStore, query: EvChargeAdjustmentQuery): MutableList<EvChargeAdjustment> {
    return query.apply(store.list())
}
