package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel surcharge cycles by code. */
interface ScheduledFuelSurchargeCycleStore {
    fun find(code: String): ScheduledFuelSurchargeCycle?
    fun save(item: ScheduledFuelSurchargeCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelSurchargeCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelSurchargeCycleStore(
    var items: MutableMap<String, ScheduledFuelSurchargeCycle>,
    var order: MutableList<String>,
) : ScheduledFuelSurchargeCycleStore {
    override fun find(code: String): ScheduledFuelSurchargeCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelSurchargeCycle) {
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

    override fun list(): MutableList<ScheduledFuelSurchargeCycle> {
        val out = mutableListOf<ScheduledFuelSurchargeCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelSurchargeCycleStore(): ScheduledFuelSurchargeCycleStore {
    return MemoryScheduledFuelSurchargeCycleStore(
        mutableMapOf<String, ScheduledFuelSurchargeCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelSurchargeCycleStore(
    var inner: ScheduledFuelSurchargeCycleStore,
    var cache: TtlCache<ScheduledFuelSurchargeCycle>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelSurchargeCycleStore {
    override fun find(code: String): ScheduledFuelSurchargeCycle? {
        val hit = (cache.get(code) as ScheduledFuelSurchargeCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelSurchargeCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelSurchargeCycle> = inner.list()
}

fun newCachedScheduledFuelSurchargeCycleStore(inner: ScheduledFuelSurchargeCycleStore, ttl: Int, clock: Clock): ScheduledFuelSurchargeCycleStore {
    return CachedScheduledFuelSurchargeCycleStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelSurchargeCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel surcharge cycles. Empty fields match all. */
data class ScheduledFuelSurchargeCycleQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelSurchargeCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledFuelSurchargeCycle>): MutableList<ScheduledFuelSurchargeCycle> {
        val matched = filter(items) { candidate: ScheduledFuelSurchargeCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelSurchargeCycles(store: ScheduledFuelSurchargeCycleStore, query: ScheduledFuelSurchargeCycleQuery): MutableList<ScheduledFuelSurchargeCycle> {
    return query.apply(store.list())
}
