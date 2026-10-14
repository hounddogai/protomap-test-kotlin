package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled trailer yard adjustments by code. */
interface ScheduledTrailerYardAdjustmentStore {
    fun find(code: String): ScheduledTrailerYardAdjustment?
    fun save(item: ScheduledTrailerYardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerYardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerYardAdjustmentStore(
    var items: MutableMap<String, ScheduledTrailerYardAdjustment>,
    var order: MutableList<String>,
) : ScheduledTrailerYardAdjustmentStore {
    override fun find(code: String): ScheduledTrailerYardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerYardAdjustment) {
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

    override fun list(): MutableList<ScheduledTrailerYardAdjustment> {
        val out = mutableListOf<ScheduledTrailerYardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerYardAdjustmentStore(): ScheduledTrailerYardAdjustmentStore {
    return MemoryScheduledTrailerYardAdjustmentStore(
        mutableMapOf<String, ScheduledTrailerYardAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerYardAdjustmentStore(
    var inner: ScheduledTrailerYardAdjustmentStore,
    var cache: TtlCache<ScheduledTrailerYardAdjustment>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerYardAdjustmentStore {
    override fun find(code: String): ScheduledTrailerYardAdjustment? {
        val hit = (cache.get(code) as ScheduledTrailerYardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerYardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerYardAdjustment> = inner.list()
}

fun newCachedScheduledTrailerYardAdjustmentStore(inner: ScheduledTrailerYardAdjustmentStore, ttl: Int, clock: Clock): ScheduledTrailerYardAdjustmentStore {
    return CachedScheduledTrailerYardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerYardAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer yard adjustments. Empty fields match all. */
data class ScheduledTrailerYardAdjustmentQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerYardAdjustment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTrailerYardAdjustment>): MutableList<ScheduledTrailerYardAdjustment> {
        val matched = filter(items) { candidate: ScheduledTrailerYardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerYardAdjustments(store: ScheduledTrailerYardAdjustmentStore, query: ScheduledTrailerYardAdjustmentQuery): MutableList<ScheduledTrailerYardAdjustment> {
    return query.apply(store.list())
}
