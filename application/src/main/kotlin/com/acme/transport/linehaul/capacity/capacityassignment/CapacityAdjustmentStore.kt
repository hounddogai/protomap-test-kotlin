package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity adjustments by code. */
interface CapacityAdjustmentStore {
    fun find(code: String): CapacityAdjustment?
    fun save(item: CapacityAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityAdjustmentStore(
    var items: MutableMap<String, CapacityAdjustment>,
    var order: MutableList<String>,
) : CapacityAdjustmentStore {
    override fun find(code: String): CapacityAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityAdjustment) {
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

    override fun list(): MutableList<CapacityAdjustment> {
        val out = mutableListOf<CapacityAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityAdjustmentStore(): CapacityAdjustmentStore {
    return MemoryCapacityAdjustmentStore(mutableMapOf<String, CapacityAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityAdjustmentStore(
    var inner: CapacityAdjustmentStore,
    var cache: TtlCache<CapacityAdjustment>,
    var hits: Int,
    var misses: Int,
) : CapacityAdjustmentStore {
    override fun find(code: String): CapacityAdjustment? {
        val hit = (cache.get(code) as CapacityAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityAdjustment> = inner.list()
}

fun newCachedCapacityAdjustmentStore(inner: CapacityAdjustmentStore, ttl: Int, clock: Clock): CapacityAdjustmentStore {
    return CachedCapacityAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CapacityAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity adjustments. Empty fields match all. */
data class CapacityAdjustmentQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityAdjustment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityAdjustment>): MutableList<CapacityAdjustment> {
        val matched = filter(items) { candidate: CapacityAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityAdjustments(store: CapacityAdjustmentStore, query: CapacityAdjustmentQuery): MutableList<CapacityAdjustment> {
    return query.apply(store.list())
}
