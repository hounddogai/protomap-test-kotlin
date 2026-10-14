package com.acme.transport.customs.hold.group

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling hold allocations by code. */
interface RollingHoldAllocationStore {
    fun find(code: String): RollingHoldAllocation?
    fun save(item: RollingHoldAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingHoldAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingHoldAllocationStore(
    var items: MutableMap<String, RollingHoldAllocation>,
    var order: MutableList<String>,
) : RollingHoldAllocationStore {
    override fun find(code: String): RollingHoldAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingHoldAllocation) {
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

    override fun list(): MutableList<RollingHoldAllocation> {
        val out = mutableListOf<RollingHoldAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingHoldAllocationStore(): RollingHoldAllocationStore {
    return MemoryRollingHoldAllocationStore(
        mutableMapOf<String, RollingHoldAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingHoldAllocationStore(
    var inner: RollingHoldAllocationStore,
    var cache: TtlCache<RollingHoldAllocation>,
    var hits: Int,
    var misses: Int,
) : RollingHoldAllocationStore {
    override fun find(code: String): RollingHoldAllocation? {
        val hit = (cache.get(code) as RollingHoldAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingHoldAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingHoldAllocation> = inner.list()
}

fun newCachedRollingHoldAllocationStore(inner: RollingHoldAllocationStore, ttl: Int, clock: Clock): RollingHoldAllocationStore {
    return CachedRollingHoldAllocationStore(
        inner = inner,
        cache = newTtlCache<RollingHoldAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling hold allocations. Empty fields match all. */
data class RollingHoldAllocationQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingHoldAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingHoldAllocation>): MutableList<RollingHoldAllocation> {
        val matched = filter(items) { candidate: RollingHoldAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingHoldAllocations(store: RollingHoldAllocationStore, query: RollingHoldAllocationQuery): MutableList<RollingHoldAllocation> {
    return query.apply(store.list())
}
