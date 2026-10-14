package com.acme.transport.safety.incident.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident allocations by code. */
interface IncidentAllocationStore {
    fun find(code: String): IncidentAllocation?
    fun save(item: IncidentAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentAllocationStore(
    var items: MutableMap<String, IncidentAllocation>,
    var order: MutableList<String>,
) : IncidentAllocationStore {
    override fun find(code: String): IncidentAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentAllocation) {
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

    override fun list(): MutableList<IncidentAllocation> {
        val out = mutableListOf<IncidentAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentAllocationStore(): IncidentAllocationStore {
    return MemoryIncidentAllocationStore(mutableMapOf<String, IncidentAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentAllocationStore(
    var inner: IncidentAllocationStore,
    var cache: TtlCache<IncidentAllocation>,
    var hits: Int,
    var misses: Int,
) : IncidentAllocationStore {
    override fun find(code: String): IncidentAllocation? {
        val hit = (cache.get(code) as IncidentAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentAllocation> = inner.list()
}

fun newCachedIncidentAllocationStore(inner: IncidentAllocationStore, ttl: Int, clock: Clock): IncidentAllocationStore {
    return CachedIncidentAllocationStore(
        inner = inner,
        cache = newTtlCache<IncidentAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident allocations. Empty fields match all. */
data class IncidentAllocationQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentAllocation>): MutableList<IncidentAllocation> {
        val matched = filter(items) { candidate: IncidentAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentAllocations(store: IncidentAllocationStore, query: IncidentAllocationQuery): MutableList<IncidentAllocation> {
    return query.apply(store.list())
}
