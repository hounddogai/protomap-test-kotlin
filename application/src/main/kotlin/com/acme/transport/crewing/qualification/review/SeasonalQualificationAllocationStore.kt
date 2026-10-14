package com.acme.transport.crewing.qualification.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal qualification allocations by code. */
interface SeasonalQualificationAllocationStore {
    fun find(code: String): SeasonalQualificationAllocation?
    fun save(item: SeasonalQualificationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalQualificationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalQualificationAllocationStore(
    var items: MutableMap<String, SeasonalQualificationAllocation>,
    var order: MutableList<String>,
) : SeasonalQualificationAllocationStore {
    override fun find(code: String): SeasonalQualificationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalQualificationAllocation) {
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

    override fun list(): MutableList<SeasonalQualificationAllocation> {
        val out = mutableListOf<SeasonalQualificationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalQualificationAllocationStore(): SeasonalQualificationAllocationStore {
    return MemorySeasonalQualificationAllocationStore(
        mutableMapOf<String, SeasonalQualificationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalQualificationAllocationStore(
    var inner: SeasonalQualificationAllocationStore,
    var cache: TtlCache<SeasonalQualificationAllocation>,
    var hits: Int,
    var misses: Int,
) : SeasonalQualificationAllocationStore {
    override fun find(code: String): SeasonalQualificationAllocation? {
        val hit = (cache.get(code) as SeasonalQualificationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalQualificationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalQualificationAllocation> = inner.list()
}

fun newCachedSeasonalQualificationAllocationStore(inner: SeasonalQualificationAllocationStore, ttl: Int, clock: Clock): SeasonalQualificationAllocationStore {
    return CachedSeasonalQualificationAllocationStore(
        inner = inner,
        cache = newTtlCache<SeasonalQualificationAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal qualification allocations. Empty fields match all. */
data class SeasonalQualificationAllocationQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalQualificationAllocation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalQualificationAllocation>): MutableList<SeasonalQualificationAllocation> {
        val matched = filter(items) { candidate: SeasonalQualificationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalQualificationAllocations(store: SeasonalQualificationAllocationStore, query: SeasonalQualificationAllocationQuery): MutableList<SeasonalQualificationAllocation> {
    return query.apply(store.list())
}
