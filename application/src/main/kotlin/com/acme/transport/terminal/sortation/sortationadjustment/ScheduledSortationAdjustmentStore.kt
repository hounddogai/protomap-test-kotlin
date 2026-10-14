package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled sortation adjustments by code. */
interface ScheduledSortationAdjustmentStore {
    fun find(code: String): ScheduledSortationAdjustment?
    fun save(item: ScheduledSortationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSortationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSortationAdjustmentStore(
    var items: MutableMap<String, ScheduledSortationAdjustment>,
    var order: MutableList<String>,
) : ScheduledSortationAdjustmentStore {
    override fun find(code: String): ScheduledSortationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSortationAdjustment) {
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

    override fun list(): MutableList<ScheduledSortationAdjustment> {
        val out = mutableListOf<ScheduledSortationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSortationAdjustmentStore(): ScheduledSortationAdjustmentStore {
    return MemoryScheduledSortationAdjustmentStore(
        mutableMapOf<String, ScheduledSortationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSortationAdjustmentStore(
    var inner: ScheduledSortationAdjustmentStore,
    var cache: TtlCache<ScheduledSortationAdjustment>,
    var hits: Int,
    var misses: Int,
) : ScheduledSortationAdjustmentStore {
    override fun find(code: String): ScheduledSortationAdjustment? {
        val hit = (cache.get(code) as ScheduledSortationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSortationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSortationAdjustment> = inner.list()
}

fun newCachedScheduledSortationAdjustmentStore(inner: ScheduledSortationAdjustmentStore, ttl: Int, clock: Clock): ScheduledSortationAdjustmentStore {
    return CachedScheduledSortationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledSortationAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled sortation adjustments. Empty fields match all. */
data class ScheduledSortationAdjustmentQuery(
    var depot: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSortationAdjustment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledSortationAdjustment>): MutableList<ScheduledSortationAdjustment> {
        val matched = filter(items) { candidate: ScheduledSortationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSortationAdjustments(store: ScheduledSortationAdjustmentStore, query: ScheduledSortationAdjustmentQuery): MutableList<ScheduledSortationAdjustment> {
    return query.apply(store.list())
}
