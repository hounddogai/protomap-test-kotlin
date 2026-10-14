package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling near miss adjustments by code. */
interface RollingNearMissAdjustmentStore {
    fun find(code: String): RollingNearMissAdjustment?
    fun save(item: RollingNearMissAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNearMissAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNearMissAdjustmentStore(
    var items: MutableMap<String, RollingNearMissAdjustment>,
    var order: MutableList<String>,
) : RollingNearMissAdjustmentStore {
    override fun find(code: String): RollingNearMissAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNearMissAdjustment) {
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

    override fun list(): MutableList<RollingNearMissAdjustment> {
        val out = mutableListOf<RollingNearMissAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNearMissAdjustmentStore(): RollingNearMissAdjustmentStore {
    return MemoryRollingNearMissAdjustmentStore(
        mutableMapOf<String, RollingNearMissAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNearMissAdjustmentStore(
    var inner: RollingNearMissAdjustmentStore,
    var cache: TtlCache<RollingNearMissAdjustment>,
    var hits: Int,
    var misses: Int,
) : RollingNearMissAdjustmentStore {
    override fun find(code: String): RollingNearMissAdjustment? {
        val hit = (cache.get(code) as RollingNearMissAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNearMissAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNearMissAdjustment> = inner.list()
}

fun newCachedRollingNearMissAdjustmentStore(inner: RollingNearMissAdjustmentStore, ttl: Int, clock: Clock): RollingNearMissAdjustmentStore {
    return CachedRollingNearMissAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RollingNearMissAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling near miss adjustments. Empty fields match all. */
data class RollingNearMissAdjustmentQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNearMissAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingNearMissAdjustment>): MutableList<RollingNearMissAdjustment> {
        val matched = filter(items) { candidate: RollingNearMissAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNearMissAdjustments(store: RollingNearMissAdjustmentStore, query: RollingNearMissAdjustmentQuery): MutableList<RollingNearMissAdjustment> {
    return query.apply(store.list())
}
