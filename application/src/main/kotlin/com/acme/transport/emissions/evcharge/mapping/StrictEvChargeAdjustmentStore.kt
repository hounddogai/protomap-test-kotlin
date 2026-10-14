package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict ev charge adjustments by code. */
interface StrictEvChargeAdjustmentStore {
    fun find(code: String): StrictEvChargeAdjustment?
    fun save(item: StrictEvChargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEvChargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEvChargeAdjustmentStore(
    var items: MutableMap<String, StrictEvChargeAdjustment>,
    var order: MutableList<String>,
) : StrictEvChargeAdjustmentStore {
    override fun find(code: String): StrictEvChargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEvChargeAdjustment) {
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

    override fun list(): MutableList<StrictEvChargeAdjustment> {
        val out = mutableListOf<StrictEvChargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEvChargeAdjustmentStore(): StrictEvChargeAdjustmentStore {
    return MemoryStrictEvChargeAdjustmentStore(
        mutableMapOf<String, StrictEvChargeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEvChargeAdjustmentStore(
    var inner: StrictEvChargeAdjustmentStore,
    var cache: TtlCache<StrictEvChargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : StrictEvChargeAdjustmentStore {
    override fun find(code: String): StrictEvChargeAdjustment? {
        val hit = (cache.get(code) as StrictEvChargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEvChargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEvChargeAdjustment> = inner.list()
}

fun newCachedStrictEvChargeAdjustmentStore(inner: StrictEvChargeAdjustmentStore, ttl: Int, clock: Clock): StrictEvChargeAdjustmentStore {
    return CachedStrictEvChargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<StrictEvChargeAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict ev charge adjustments. Empty fields match all. */
data class StrictEvChargeAdjustmentQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEvChargeAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictEvChargeAdjustment>): MutableList<StrictEvChargeAdjustment> {
        val matched = filter(items) { candidate: StrictEvChargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEvChargeAdjustments(store: StrictEvChargeAdjustmentStore, query: StrictEvChargeAdjustmentQuery): MutableList<StrictEvChargeAdjustment> {
    return query.apply(store.list())
}
