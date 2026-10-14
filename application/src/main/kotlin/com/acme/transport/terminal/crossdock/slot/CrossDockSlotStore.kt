package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock slots by code. */
interface CrossDockSlotStore {
    fun find(code: String): CrossDockSlot?
    fun save(item: CrossDockSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockSlotStore(
    var items: MutableMap<String, CrossDockSlot>,
    var order: MutableList<String>,
) : CrossDockSlotStore {
    override fun find(code: String): CrossDockSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockSlot) {
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

    override fun list(): MutableList<CrossDockSlot> {
        val out = mutableListOf<CrossDockSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockSlotStore(): CrossDockSlotStore {
    return MemoryCrossDockSlotStore(mutableMapOf<String, CrossDockSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockSlotStore(
    var inner: CrossDockSlotStore,
    var cache: TtlCache<CrossDockSlot>,
    var hits: Int,
    var misses: Int,
) : CrossDockSlotStore {
    override fun find(code: String): CrossDockSlot? {
        val hit = (cache.get(code) as CrossDockSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockSlot> = inner.list()
}

fun newCachedCrossDockSlotStore(inner: CrossDockSlotStore, ttl: Int, clock: Clock): CrossDockSlotStore {
    return CachedCrossDockSlotStore(
        inner = inner,
        cache = newTtlCache<CrossDockSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock slots. Empty fields match all. */
data class CrossDockSlotQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CrossDockSlot>): MutableList<CrossDockSlot> {
        val matched = filter(items) { candidate: CrossDockSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockSlots(store: CrossDockSlotStore, query: CrossDockSlotQuery): MutableList<CrossDockSlot> {
    return query.apply(store.list())
}
