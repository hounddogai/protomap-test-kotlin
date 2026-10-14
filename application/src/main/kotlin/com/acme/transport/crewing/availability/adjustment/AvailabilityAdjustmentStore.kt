package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability adjustments by code. */
interface AvailabilityAdjustmentStore {
    fun find(code: String): AvailabilityAdjustment?
    fun save(item: AvailabilityAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityAdjustmentStore(
    var items: MutableMap<String, AvailabilityAdjustment>,
    var order: MutableList<String>,
) : AvailabilityAdjustmentStore {
    override fun find(code: String): AvailabilityAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityAdjustment) {
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

    override fun list(): MutableList<AvailabilityAdjustment> {
        val out = mutableListOf<AvailabilityAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityAdjustmentStore(): AvailabilityAdjustmentStore {
    return MemoryAvailabilityAdjustmentStore(
        mutableMapOf<String, AvailabilityAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityAdjustmentStore(
    var inner: AvailabilityAdjustmentStore,
    var cache: TtlCache<AvailabilityAdjustment>,
    var hits: Int,
    var misses: Int,
) : AvailabilityAdjustmentStore {
    override fun find(code: String): AvailabilityAdjustment? {
        val hit = (cache.get(code) as AvailabilityAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityAdjustment> = inner.list()
}

fun newCachedAvailabilityAdjustmentStore(inner: AvailabilityAdjustmentStore, ttl: Int, clock: Clock): AvailabilityAdjustmentStore {
    return CachedAvailabilityAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AvailabilityAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability adjustments. Empty fields match all. */
data class AvailabilityAdjustmentQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityAdjustment>): MutableList<AvailabilityAdjustment> {
        val matched = filter(items) { candidate: AvailabilityAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityAdjustments(store: AvailabilityAdjustmentStore, query: AvailabilityAdjustmentQuery): MutableList<AvailabilityAdjustment> {
    return query.apply(store.list())
}
