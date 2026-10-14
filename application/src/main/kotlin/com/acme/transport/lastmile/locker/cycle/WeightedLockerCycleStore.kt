package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted locker cycles by code. */
interface WeightedLockerCycleStore {
    fun find(code: String): WeightedLockerCycle?
    fun save(item: WeightedLockerCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLockerCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLockerCycleStore(
    var items: MutableMap<String, WeightedLockerCycle>,
    var order: MutableList<String>,
) : WeightedLockerCycleStore {
    override fun find(code: String): WeightedLockerCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLockerCycle) {
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

    override fun list(): MutableList<WeightedLockerCycle> {
        val out = mutableListOf<WeightedLockerCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLockerCycleStore(): WeightedLockerCycleStore {
    return MemoryWeightedLockerCycleStore(mutableMapOf<String, WeightedLockerCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLockerCycleStore(
    var inner: WeightedLockerCycleStore,
    var cache: TtlCache<WeightedLockerCycle>,
    var hits: Int,
    var misses: Int,
) : WeightedLockerCycleStore {
    override fun find(code: String): WeightedLockerCycle? {
        val hit = (cache.get(code) as WeightedLockerCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLockerCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLockerCycle> = inner.list()
}

fun newCachedWeightedLockerCycleStore(inner: WeightedLockerCycleStore, ttl: Int, clock: Clock): WeightedLockerCycleStore {
    return CachedWeightedLockerCycleStore(
        inner = inner,
        cache = newTtlCache<WeightedLockerCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted locker cycles. Empty fields match all. */
data class WeightedLockerCycleQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLockerCycle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLockerCycle>): MutableList<WeightedLockerCycle> {
        val matched = filter(items) { candidate: WeightedLockerCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLockerCycles(store: WeightedLockerCycleStore, query: WeightedLockerCycleQuery): MutableList<WeightedLockerCycle> {
    return query.apply(store.list())
}
