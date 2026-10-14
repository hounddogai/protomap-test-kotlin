package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual fuel surcharge segments by code. */
interface ManualFuelSurchargeSegmentStore {
    fun find(code: String): ManualFuelSurchargeSegment?
    fun save(item: ManualFuelSurchargeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFuelSurchargeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFuelSurchargeSegmentStore(
    var items: MutableMap<String, ManualFuelSurchargeSegment>,
    var order: MutableList<String>,
) : ManualFuelSurchargeSegmentStore {
    override fun find(code: String): ManualFuelSurchargeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFuelSurchargeSegment) {
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

    override fun list(): MutableList<ManualFuelSurchargeSegment> {
        val out = mutableListOf<ManualFuelSurchargeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFuelSurchargeSegmentStore(): ManualFuelSurchargeSegmentStore {
    return MemoryManualFuelSurchargeSegmentStore(
        mutableMapOf<String, ManualFuelSurchargeSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFuelSurchargeSegmentStore(
    var inner: ManualFuelSurchargeSegmentStore,
    var cache: TtlCache<ManualFuelSurchargeSegment>,
    var hits: Int,
    var misses: Int,
) : ManualFuelSurchargeSegmentStore {
    override fun find(code: String): ManualFuelSurchargeSegment? {
        val hit = (cache.get(code) as ManualFuelSurchargeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFuelSurchargeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFuelSurchargeSegment> = inner.list()
}

fun newCachedManualFuelSurchargeSegmentStore(inner: ManualFuelSurchargeSegmentStore, ttl: Int, clock: Clock): ManualFuelSurchargeSegmentStore {
    return CachedManualFuelSurchargeSegmentStore(
        inner = inner,
        cache = newTtlCache<ManualFuelSurchargeSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual fuel surcharge segments. Empty fields match all. */
data class ManualFuelSurchargeSegmentQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFuelSurchargeSegment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFuelSurchargeSegment>): MutableList<ManualFuelSurchargeSegment> {
        val matched = filter(items) { candidate: ManualFuelSurchargeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFuelSurchargeSegments(store: ManualFuelSurchargeSegmentStore, query: ManualFuelSurchargeSegmentQuery): MutableList<ManualFuelSurchargeSegment> {
    return query.apply(store.list())
}
