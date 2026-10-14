package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal violation allocations by code. */
interface SeasonalViolationAllocationStore {
    fun find(code: String): SeasonalViolationAllocation?
    fun save(item: SeasonalViolationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalViolationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalViolationAllocationStore(
    var items: MutableMap<String, SeasonalViolationAllocation>,
    var order: MutableList<String>,
) : SeasonalViolationAllocationStore {
    override fun find(code: String): SeasonalViolationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalViolationAllocation) {
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

    override fun list(): MutableList<SeasonalViolationAllocation> {
        val out = mutableListOf<SeasonalViolationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalViolationAllocationStore(): SeasonalViolationAllocationStore {
    return MemorySeasonalViolationAllocationStore(
        mutableMapOf<String, SeasonalViolationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalViolationAllocationStore(
    var inner: SeasonalViolationAllocationStore,
    var cache: TtlCache<SeasonalViolationAllocation>,
    var hits: Int,
    var misses: Int,
) : SeasonalViolationAllocationStore {
    override fun find(code: String): SeasonalViolationAllocation? {
        val hit = (cache.get(code) as SeasonalViolationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalViolationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalViolationAllocation> = inner.list()
}

fun newCachedSeasonalViolationAllocationStore(inner: SeasonalViolationAllocationStore, ttl: Int, clock: Clock): SeasonalViolationAllocationStore {
    return CachedSeasonalViolationAllocationStore(
        inner = inner,
        cache = newTtlCache<SeasonalViolationAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal violation allocations. Empty fields match all. */
data class SeasonalViolationAllocationQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalViolationAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalViolationAllocation>): MutableList<SeasonalViolationAllocation> {
        val matched = filter(items) { candidate: SeasonalViolationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalViolationAllocations(store: SeasonalViolationAllocationStore, query: SeasonalViolationAllocationQuery): MutableList<SeasonalViolationAllocation> {
    return query.apply(store.list())
}
