package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered camera event allocations by code. */
interface TieredCameraEventAllocationStore {
    fun find(code: String): TieredCameraEventAllocation?
    fun save(item: TieredCameraEventAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCameraEventAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCameraEventAllocationStore(
    var items: MutableMap<String, TieredCameraEventAllocation>,
    var order: MutableList<String>,
) : TieredCameraEventAllocationStore {
    override fun find(code: String): TieredCameraEventAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCameraEventAllocation) {
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

    override fun list(): MutableList<TieredCameraEventAllocation> {
        val out = mutableListOf<TieredCameraEventAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCameraEventAllocationStore(): TieredCameraEventAllocationStore {
    return MemoryTieredCameraEventAllocationStore(
        mutableMapOf<String, TieredCameraEventAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCameraEventAllocationStore(
    var inner: TieredCameraEventAllocationStore,
    var cache: TtlCache<TieredCameraEventAllocation>,
    var hits: Int,
    var misses: Int,
) : TieredCameraEventAllocationStore {
    override fun find(code: String): TieredCameraEventAllocation? {
        val hit = (cache.get(code) as TieredCameraEventAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCameraEventAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCameraEventAllocation> = inner.list()
}

fun newCachedTieredCameraEventAllocationStore(inner: TieredCameraEventAllocationStore, ttl: Int, clock: Clock): TieredCameraEventAllocationStore {
    return CachedTieredCameraEventAllocationStore(
        inner = inner,
        cache = newTtlCache<TieredCameraEventAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered camera event allocations. Empty fields match all. */
data class TieredCameraEventAllocationQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCameraEventAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCameraEventAllocation>): MutableList<TieredCameraEventAllocation> {
        val matched = filter(items) { candidate: TieredCameraEventAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCameraEventAllocations(store: TieredCameraEventAllocationStore, query: TieredCameraEventAllocationQuery): MutableList<TieredCameraEventAllocation> {
    return query.apply(store.list())
}
