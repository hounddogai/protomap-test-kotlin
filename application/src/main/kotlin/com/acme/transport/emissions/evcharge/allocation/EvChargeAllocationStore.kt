package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge allocations by code. */
interface EvChargeAllocationStore {
    fun find(code: String): EvChargeAllocation?
    fun save(item: EvChargeAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeAllocationStore(
    var items: MutableMap<String, EvChargeAllocation>,
    var order: MutableList<String>,
) : EvChargeAllocationStore {
    override fun find(code: String): EvChargeAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeAllocation) {
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

    override fun list(): MutableList<EvChargeAllocation> {
        val out = mutableListOf<EvChargeAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeAllocationStore(): EvChargeAllocationStore {
    return MemoryEvChargeAllocationStore(mutableMapOf<String, EvChargeAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeAllocationStore(
    var inner: EvChargeAllocationStore,
    var cache: TtlCache<EvChargeAllocation>,
    var hits: Int,
    var misses: Int,
) : EvChargeAllocationStore {
    override fun find(code: String): EvChargeAllocation? {
        val hit = (cache.get(code) as EvChargeAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeAllocation> = inner.list()
}

fun newCachedEvChargeAllocationStore(inner: EvChargeAllocationStore, ttl: Int, clock: Clock): EvChargeAllocationStore {
    return CachedEvChargeAllocationStore(
        inner = inner,
        cache = newTtlCache<EvChargeAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge allocations. Empty fields match all. */
data class EvChargeAllocationQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeAllocation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<EvChargeAllocation>): MutableList<EvChargeAllocation> {
        val matched = filter(items) { candidate: EvChargeAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeAllocations(store: EvChargeAllocationStore, query: EvChargeAllocationQuery): MutableList<EvChargeAllocation> {
    return query.apply(store.list())
}
