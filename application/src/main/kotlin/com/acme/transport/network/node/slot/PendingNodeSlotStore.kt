package com.acme.transport.network.node.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node slots by code. */
interface PendingNodeSlotStore {
    fun find(code: String): PendingNodeSlot?
    fun save(item: PendingNodeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodeSlotStore(
    var items: MutableMap<String, PendingNodeSlot>,
    var order: MutableList<String>,
) : PendingNodeSlotStore {
    override fun find(code: String): PendingNodeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodeSlot) {
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

    override fun list(): MutableList<PendingNodeSlot> {
        val out = mutableListOf<PendingNodeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodeSlotStore(): PendingNodeSlotStore {
    return MemoryPendingNodeSlotStore(mutableMapOf<String, PendingNodeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodeSlotStore(
    var inner: PendingNodeSlotStore,
    var cache: TtlCache<PendingNodeSlot>,
    var hits: Int,
    var misses: Int,
) : PendingNodeSlotStore {
    override fun find(code: String): PendingNodeSlot? {
        val hit = (cache.get(code) as PendingNodeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodeSlot> = inner.list()
}

fun newCachedPendingNodeSlotStore(inner: PendingNodeSlotStore, ttl: Int, clock: Clock): PendingNodeSlotStore {
    return CachedPendingNodeSlotStore(
        inner = inner,
        cache = newTtlCache<PendingNodeSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node slots. Empty fields match all. */
data class PendingNodeSlotQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodeSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNodeSlot>): MutableList<PendingNodeSlot> {
        val matched = filter(items) { candidate: PendingNodeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodeSlots(store: PendingNodeSlotStore, query: PendingNodeSlotQuery): MutableList<PendingNodeSlot> {
    return query.apply(store.list())
}
