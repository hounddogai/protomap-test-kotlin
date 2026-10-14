package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss allocations by code. */
interface NearMissAllocationStore {
    fun find(code: String): NearMissAllocation?
    fun save(item: NearMissAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissAllocationStore(
    var items: MutableMap<String, NearMissAllocation>,
    var order: MutableList<String>,
) : NearMissAllocationStore {
    override fun find(code: String): NearMissAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissAllocation) {
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

    override fun list(): MutableList<NearMissAllocation> {
        val out = mutableListOf<NearMissAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissAllocationStore(): NearMissAllocationStore {
    return MemoryNearMissAllocationStore(mutableMapOf<String, NearMissAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissAllocationStore(
    var inner: NearMissAllocationStore,
    var cache: TtlCache<NearMissAllocation>,
    var hits: Int,
    var misses: Int,
) : NearMissAllocationStore {
    override fun find(code: String): NearMissAllocation? {
        val hit = (cache.get(code) as NearMissAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissAllocation> = inner.list()
}

fun newCachedNearMissAllocationStore(inner: NearMissAllocationStore, ttl: Int, clock: Clock): NearMissAllocationStore {
    return CachedNearMissAllocationStore(
        inner = inner,
        cache = newTtlCache<NearMissAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss allocations. Empty fields match all. */
data class NearMissAllocationQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<NearMissAllocation>): MutableList<NearMissAllocation> {
        val matched = filter(items) { candidate: NearMissAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissAllocations(store: NearMissAllocationStore, query: NearMissAllocationQuery): MutableList<NearMissAllocation> {
    return query.apply(store.list())
}
