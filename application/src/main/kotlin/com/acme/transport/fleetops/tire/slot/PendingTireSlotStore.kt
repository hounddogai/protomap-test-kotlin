package com.acme.transport.fleetops.tire.slot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tire slots by code. */
interface PendingTireSlotStore {
    fun find(code: String): PendingTireSlot?
    fun save(item: PendingTireSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTireSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTireSlotStore(
    var items: MutableMap<String, PendingTireSlot>,
    var order: MutableList<String>,
) : PendingTireSlotStore {
    override fun find(code: String): PendingTireSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTireSlot) {
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

    override fun list(): MutableList<PendingTireSlot> {
        val out = mutableListOf<PendingTireSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTireSlotStore(): PendingTireSlotStore {
    return MemoryPendingTireSlotStore(mutableMapOf<String, PendingTireSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTireSlotStore(
    var inner: PendingTireSlotStore,
    var cache: TtlCache<PendingTireSlot>,
    var hits: Int,
    var misses: Int,
) : PendingTireSlotStore {
    override fun find(code: String): PendingTireSlot? {
        val hit = (cache.get(code) as PendingTireSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTireSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTireSlot> = inner.list()
}

fun newCachedPendingTireSlotStore(inner: PendingTireSlotStore, ttl: Int, clock: Clock): PendingTireSlotStore {
    return CachedPendingTireSlotStore(
        inner = inner,
        cache = newTtlCache<PendingTireSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tire slots. Empty fields match all. */
data class PendingTireSlotQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTireSlot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTireSlot>): MutableList<PendingTireSlot> {
        val matched = filter(items) { candidate: PendingTireSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTireSlots(store: PendingTireSlotStore, query: PendingTireSlotQuery): MutableList<PendingTireSlot> {
    return query.apply(store.list())
}
