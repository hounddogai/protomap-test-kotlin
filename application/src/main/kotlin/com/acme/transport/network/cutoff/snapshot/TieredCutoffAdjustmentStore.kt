package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered cutoff adjustments by code. */
interface TieredCutoffAdjustmentStore {
    fun find(code: String): TieredCutoffAdjustment?
    fun save(item: TieredCutoffAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCutoffAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCutoffAdjustmentStore(
    var items: MutableMap<String, TieredCutoffAdjustment>,
    var order: MutableList<String>,
) : TieredCutoffAdjustmentStore {
    override fun find(code: String): TieredCutoffAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCutoffAdjustment) {
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

    override fun list(): MutableList<TieredCutoffAdjustment> {
        val out = mutableListOf<TieredCutoffAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCutoffAdjustmentStore(): TieredCutoffAdjustmentStore {
    return MemoryTieredCutoffAdjustmentStore(
        mutableMapOf<String, TieredCutoffAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCutoffAdjustmentStore(
    var inner: TieredCutoffAdjustmentStore,
    var cache: TtlCache<TieredCutoffAdjustment>,
    var hits: Int,
    var misses: Int,
) : TieredCutoffAdjustmentStore {
    override fun find(code: String): TieredCutoffAdjustment? {
        val hit = (cache.get(code) as TieredCutoffAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCutoffAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCutoffAdjustment> = inner.list()
}

fun newCachedTieredCutoffAdjustmentStore(inner: TieredCutoffAdjustmentStore, ttl: Int, clock: Clock): TieredCutoffAdjustmentStore {
    return CachedTieredCutoffAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TieredCutoffAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cutoff adjustments. Empty fields match all. */
data class TieredCutoffAdjustmentQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCutoffAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TieredCutoffAdjustment>): MutableList<TieredCutoffAdjustment> {
        val matched = filter(items) { candidate: TieredCutoffAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCutoffAdjustments(store: TieredCutoffAdjustmentStore, query: TieredCutoffAdjustmentQuery): MutableList<TieredCutoffAdjustment> {
    return query.apply(store.list())
}
