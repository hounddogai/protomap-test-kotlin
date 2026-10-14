package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary hold adjustments by code. */
interface PrimaryHoldAdjustmentStore {
    fun find(code: String): PrimaryHoldAdjustment?
    fun save(item: PrimaryHoldAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryHoldAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryHoldAdjustmentStore(
    var items: MutableMap<String, PrimaryHoldAdjustment>,
    var order: MutableList<String>,
) : PrimaryHoldAdjustmentStore {
    override fun find(code: String): PrimaryHoldAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryHoldAdjustment) {
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

    override fun list(): MutableList<PrimaryHoldAdjustment> {
        val out = mutableListOf<PrimaryHoldAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryHoldAdjustmentStore(): PrimaryHoldAdjustmentStore {
    return MemoryPrimaryHoldAdjustmentStore(
        mutableMapOf<String, PrimaryHoldAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryHoldAdjustmentStore(
    var inner: PrimaryHoldAdjustmentStore,
    var cache: TtlCache<PrimaryHoldAdjustment>,
    var hits: Int,
    var misses: Int,
) : PrimaryHoldAdjustmentStore {
    override fun find(code: String): PrimaryHoldAdjustment? {
        val hit = (cache.get(code) as PrimaryHoldAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryHoldAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryHoldAdjustment> = inner.list()
}

fun newCachedPrimaryHoldAdjustmentStore(inner: PrimaryHoldAdjustmentStore, ttl: Int, clock: Clock): PrimaryHoldAdjustmentStore {
    return CachedPrimaryHoldAdjustmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryHoldAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary hold adjustments. Empty fields match all. */
data class PrimaryHoldAdjustmentQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryHoldAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryHoldAdjustment>): MutableList<PrimaryHoldAdjustment> {
        val matched = filter(items) { candidate: PrimaryHoldAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryHoldAdjustments(store: PrimaryHoldAdjustmentStore, query: PrimaryHoldAdjustmentQuery): MutableList<PrimaryHoldAdjustment> {
    return query.apply(store.list())
}
