package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling cutoff allocations by code. */
interface RollingCutoffAllocationStore {
    fun find(code: String): RollingCutoffAllocation?
    fun save(item: RollingCutoffAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCutoffAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCutoffAllocationStore(
    var items: MutableMap<String, RollingCutoffAllocation>,
    var order: MutableList<String>,
) : RollingCutoffAllocationStore {
    override fun find(code: String): RollingCutoffAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCutoffAllocation) {
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

    override fun list(): MutableList<RollingCutoffAllocation> {
        val out = mutableListOf<RollingCutoffAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCutoffAllocationStore(): RollingCutoffAllocationStore {
    return MemoryRollingCutoffAllocationStore(
        mutableMapOf<String, RollingCutoffAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCutoffAllocationStore(
    var inner: RollingCutoffAllocationStore,
    var cache: TtlCache<RollingCutoffAllocation>,
    var hits: Int,
    var misses: Int,
) : RollingCutoffAllocationStore {
    override fun find(code: String): RollingCutoffAllocation? {
        val hit = (cache.get(code) as RollingCutoffAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCutoffAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCutoffAllocation> = inner.list()
}

fun newCachedRollingCutoffAllocationStore(inner: RollingCutoffAllocationStore, ttl: Int, clock: Clock): RollingCutoffAllocationStore {
    return CachedRollingCutoffAllocationStore(
        inner = inner,
        cache = newTtlCache<RollingCutoffAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling cutoff allocations. Empty fields match all. */
data class RollingCutoffAllocationQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCutoffAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCutoffAllocation>): MutableList<RollingCutoffAllocation> {
        val matched = filter(items) { candidate: RollingCutoffAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCutoffAllocations(store: RollingCutoffAllocationStore, query: RollingCutoffAllocationQuery): MutableList<RollingCutoffAllocation> {
    return query.apply(store.list())
}
