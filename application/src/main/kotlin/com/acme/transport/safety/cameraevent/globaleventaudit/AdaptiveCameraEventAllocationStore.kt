package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive camera event allocations by code. */
interface AdaptiveCameraEventAllocationStore {
    fun find(code: String): AdaptiveCameraEventAllocation?
    fun save(item: AdaptiveCameraEventAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCameraEventAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCameraEventAllocationStore(
    var items: MutableMap<String, AdaptiveCameraEventAllocation>,
    var order: MutableList<String>,
) : AdaptiveCameraEventAllocationStore {
    override fun find(code: String): AdaptiveCameraEventAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCameraEventAllocation) {
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

    override fun list(): MutableList<AdaptiveCameraEventAllocation> {
        val out = mutableListOf<AdaptiveCameraEventAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCameraEventAllocationStore(): AdaptiveCameraEventAllocationStore {
    return MemoryAdaptiveCameraEventAllocationStore(
        mutableMapOf<String, AdaptiveCameraEventAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCameraEventAllocationStore(
    var inner: AdaptiveCameraEventAllocationStore,
    var cache: TtlCache<AdaptiveCameraEventAllocation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCameraEventAllocationStore {
    override fun find(code: String): AdaptiveCameraEventAllocation? {
        val hit = (cache.get(code) as AdaptiveCameraEventAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCameraEventAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCameraEventAllocation> = inner.list()
}

fun newCachedAdaptiveCameraEventAllocationStore(inner: AdaptiveCameraEventAllocationStore, ttl: Int, clock: Clock): AdaptiveCameraEventAllocationStore {
    return CachedAdaptiveCameraEventAllocationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCameraEventAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive camera event allocations. Empty fields match all. */
data class AdaptiveCameraEventAllocationQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCameraEventAllocation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCameraEventAllocation>): MutableList<AdaptiveCameraEventAllocation> {
        val matched = filter(items) { candidate: AdaptiveCameraEventAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCameraEventAllocations(store: AdaptiveCameraEventAllocationStore, query: AdaptiveCameraEventAllocationQuery): MutableList<AdaptiveCameraEventAllocation> {
    return query.apply(store.list())
}
