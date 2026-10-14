package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling qualification allocations by code. */
interface RollingQualificationAllocationStore {
    fun find(code: String): RollingQualificationAllocation?
    fun save(item: RollingQualificationAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingQualificationAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingQualificationAllocationStore(
    var items: MutableMap<String, RollingQualificationAllocation>,
    var order: MutableList<String>,
) : RollingQualificationAllocationStore {
    override fun find(code: String): RollingQualificationAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingQualificationAllocation) {
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

    override fun list(): MutableList<RollingQualificationAllocation> {
        val out = mutableListOf<RollingQualificationAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingQualificationAllocationStore(): RollingQualificationAllocationStore {
    return MemoryRollingQualificationAllocationStore(
        mutableMapOf<String, RollingQualificationAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingQualificationAllocationStore(
    var inner: RollingQualificationAllocationStore,
    var cache: TtlCache<RollingQualificationAllocation>,
    var hits: Int,
    var misses: Int,
) : RollingQualificationAllocationStore {
    override fun find(code: String): RollingQualificationAllocation? {
        val hit = (cache.get(code) as RollingQualificationAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingQualificationAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingQualificationAllocation> = inner.list()
}

fun newCachedRollingQualificationAllocationStore(inner: RollingQualificationAllocationStore, ttl: Int, clock: Clock): RollingQualificationAllocationStore {
    return CachedRollingQualificationAllocationStore(
        inner = inner,
        cache = newTtlCache<RollingQualificationAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling qualification allocations. Empty fields match all. */
data class RollingQualificationAllocationQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingQualificationAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingQualificationAllocation>): MutableList<RollingQualificationAllocation> {
        val matched = filter(items) { candidate: RollingQualificationAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingQualificationAllocations(store: RollingQualificationAllocationStore, query: RollingQualificationAllocationQuery): MutableList<RollingQualificationAllocation> {
    return query.apply(store.list())
}
