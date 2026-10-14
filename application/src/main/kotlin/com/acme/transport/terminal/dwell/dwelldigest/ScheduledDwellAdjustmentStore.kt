package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dwell adjustments by code. */
interface ScheduledDwellAdjustmentStore {
    fun find(code: String): ScheduledDwellAdjustment?
    fun save(item: ScheduledDwellAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDwellAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDwellAdjustmentStore(
    var items: MutableMap<String, ScheduledDwellAdjustment>,
    var order: MutableList<String>,
) : ScheduledDwellAdjustmentStore {
    override fun find(code: String): ScheduledDwellAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDwellAdjustment) {
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

    override fun list(): MutableList<ScheduledDwellAdjustment> {
        val out = mutableListOf<ScheduledDwellAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDwellAdjustmentStore(): ScheduledDwellAdjustmentStore {
    return MemoryScheduledDwellAdjustmentStore(
        mutableMapOf<String, ScheduledDwellAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDwellAdjustmentStore(
    var inner: ScheduledDwellAdjustmentStore,
    var cache: TtlCache<ScheduledDwellAdjustment>,
    var hits: Int,
    var misses: Int,
) : ScheduledDwellAdjustmentStore {
    override fun find(code: String): ScheduledDwellAdjustment? {
        val hit = (cache.get(code) as ScheduledDwellAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDwellAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDwellAdjustment> = inner.list()
}

fun newCachedScheduledDwellAdjustmentStore(inner: ScheduledDwellAdjustmentStore, ttl: Int, clock: Clock): ScheduledDwellAdjustmentStore {
    return CachedScheduledDwellAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledDwellAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dwell adjustments. Empty fields match all. */
data class ScheduledDwellAdjustmentQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDwellAdjustment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDwellAdjustment>): MutableList<ScheduledDwellAdjustment> {
        val matched = filter(items) { candidate: ScheduledDwellAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDwellAdjustments(store: ScheduledDwellAdjustmentStore, query: ScheduledDwellAdjustmentQuery): MutableList<ScheduledDwellAdjustment> {
    return query.apply(store.list())
}
