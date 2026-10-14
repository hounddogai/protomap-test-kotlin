package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan allocations by code. */
interface AdaptiveLanePlanAllocationStore {
    fun find(code: String): AdaptiveLanePlanAllocation?
    fun save(item: AdaptiveLanePlanAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanAllocationStore(
    var items: MutableMap<String, AdaptiveLanePlanAllocation>,
    var order: MutableList<String>,
) : AdaptiveLanePlanAllocationStore {
    override fun find(code: String): AdaptiveLanePlanAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanAllocation) {
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

    override fun list(): MutableList<AdaptiveLanePlanAllocation> {
        val out = mutableListOf<AdaptiveLanePlanAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanAllocationStore(): AdaptiveLanePlanAllocationStore {
    return MemoryAdaptiveLanePlanAllocationStore(
        mutableMapOf<String, AdaptiveLanePlanAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanAllocationStore(
    var inner: AdaptiveLanePlanAllocationStore,
    var cache: TtlCache<AdaptiveLanePlanAllocation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanAllocationStore {
    override fun find(code: String): AdaptiveLanePlanAllocation? {
        val hit = (cache.get(code) as AdaptiveLanePlanAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanAllocation> = inner.list()
}

fun newCachedAdaptiveLanePlanAllocationStore(inner: AdaptiveLanePlanAllocationStore, ttl: Int, clock: Clock): AdaptiveLanePlanAllocationStore {
    return CachedAdaptiveLanePlanAllocationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan allocations. Empty fields match all. */
data class AdaptiveLanePlanAllocationQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveLanePlanAllocation>): MutableList<AdaptiveLanePlanAllocation> {
        val matched = filter(items) { candidate: AdaptiveLanePlanAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanAllocations(store: AdaptiveLanePlanAllocationStore, query: AdaptiveLanePlanAllocationQuery): MutableList<AdaptiveLanePlanAllocation> {
    return query.apply(store.list())
}
