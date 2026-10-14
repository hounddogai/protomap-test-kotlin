package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching adjustments by code. */
interface CoachingAdjustmentStore {
    fun find(code: String): CoachingAdjustment?
    fun save(item: CoachingAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingAdjustmentStore(
    var items: MutableMap<String, CoachingAdjustment>,
    var order: MutableList<String>,
) : CoachingAdjustmentStore {
    override fun find(code: String): CoachingAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingAdjustment) {
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

    override fun list(): MutableList<CoachingAdjustment> {
        val out = mutableListOf<CoachingAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingAdjustmentStore(): CoachingAdjustmentStore {
    return MemoryCoachingAdjustmentStore(mutableMapOf<String, CoachingAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingAdjustmentStore(
    var inner: CoachingAdjustmentStore,
    var cache: TtlCache<CoachingAdjustment>,
    var hits: Int,
    var misses: Int,
) : CoachingAdjustmentStore {
    override fun find(code: String): CoachingAdjustment? {
        val hit = (cache.get(code) as CoachingAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingAdjustment> = inner.list()
}

fun newCachedCoachingAdjustmentStore(inner: CoachingAdjustmentStore, ttl: Int, clock: Clock): CoachingAdjustmentStore {
    return CachedCoachingAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CoachingAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching adjustments. Empty fields match all. */
data class CoachingAdjustmentQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingAdjustment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CoachingAdjustment>): MutableList<CoachingAdjustment> {
        val matched = filter(items) { candidate: CoachingAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingAdjustments(store: CoachingAdjustmentStore, query: CoachingAdjustmentQuery): MutableList<CoachingAdjustment> {
    return query.apply(store.list())
}
