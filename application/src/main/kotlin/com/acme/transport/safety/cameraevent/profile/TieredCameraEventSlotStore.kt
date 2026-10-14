package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered camera event slots by code. */
interface TieredCameraEventSlotStore {
    fun find(code: String): TieredCameraEventSlot?
    fun save(item: TieredCameraEventSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCameraEventSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCameraEventSlotStore(
    var items: MutableMap<String, TieredCameraEventSlot>,
    var order: MutableList<String>,
) : TieredCameraEventSlotStore {
    override fun find(code: String): TieredCameraEventSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCameraEventSlot) {
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

    override fun list(): MutableList<TieredCameraEventSlot> {
        val out = mutableListOf<TieredCameraEventSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCameraEventSlotStore(): TieredCameraEventSlotStore {
    return MemoryTieredCameraEventSlotStore(
        mutableMapOf<String, TieredCameraEventSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCameraEventSlotStore(
    var inner: TieredCameraEventSlotStore,
    var cache: TtlCache<TieredCameraEventSlot>,
    var hits: Int,
    var misses: Int,
) : TieredCameraEventSlotStore {
    override fun find(code: String): TieredCameraEventSlot? {
        val hit = (cache.get(code) as TieredCameraEventSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCameraEventSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCameraEventSlot> = inner.list()
}

fun newCachedTieredCameraEventSlotStore(inner: TieredCameraEventSlotStore, ttl: Int, clock: Clock): TieredCameraEventSlotStore {
    return CachedTieredCameraEventSlotStore(
        inner = inner,
        cache = newTtlCache<TieredCameraEventSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered camera event slots. Empty fields match all. */
data class TieredCameraEventSlotQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCameraEventSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCameraEventSlot>): MutableList<TieredCameraEventSlot> {
        val matched = filter(items) { candidate: TieredCameraEventSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCameraEventSlots(store: TieredCameraEventSlotStore, query: TieredCameraEventSlotQuery): MutableList<TieredCameraEventSlot> {
    return query.apply(store.list())
}
