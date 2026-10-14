package com.acme.transport.fleetops.tire.draft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire adjustments by code. */
interface TireAdjustmentStore {
    fun find(code: String): TireAdjustment?
    fun save(item: TireAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireAdjustmentStore(
    var items: MutableMap<String, TireAdjustment>,
    var order: MutableList<String>,
) : TireAdjustmentStore {
    override fun find(code: String): TireAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireAdjustment) {
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

    override fun list(): MutableList<TireAdjustment> {
        val out = mutableListOf<TireAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireAdjustmentStore(): TireAdjustmentStore {
    return MemoryTireAdjustmentStore(mutableMapOf<String, TireAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireAdjustmentStore(
    var inner: TireAdjustmentStore,
    var cache: TtlCache<TireAdjustment>,
    var hits: Int,
    var misses: Int,
) : TireAdjustmentStore {
    override fun find(code: String): TireAdjustment? {
        val hit = (cache.get(code) as TireAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireAdjustment> = inner.list()
}

fun newCachedTireAdjustmentStore(inner: TireAdjustmentStore, ttl: Int, clock: Clock): TireAdjustmentStore {
    return CachedTireAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TireAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire adjustments. Empty fields match all. */
data class TireAdjustmentQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireAdjustment>): MutableList<TireAdjustment> {
        val matched = filter(items) { candidate: TireAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireAdjustments(store: TireAdjustmentStore, query: TireAdjustmentQuery): MutableList<TireAdjustment> {
    return query.apply(store.list())
}
