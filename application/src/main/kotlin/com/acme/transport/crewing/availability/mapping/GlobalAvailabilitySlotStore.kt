package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global availability slots by code. */
interface GlobalAvailabilitySlotStore {
    fun find(code: String): GlobalAvailabilitySlot?
    fun save(item: GlobalAvailabilitySlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAvailabilitySlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAvailabilitySlotStore(
    var items: MutableMap<String, GlobalAvailabilitySlot>,
    var order: MutableList<String>,
) : GlobalAvailabilitySlotStore {
    override fun find(code: String): GlobalAvailabilitySlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAvailabilitySlot) {
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

    override fun list(): MutableList<GlobalAvailabilitySlot> {
        val out = mutableListOf<GlobalAvailabilitySlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAvailabilitySlotStore(): GlobalAvailabilitySlotStore {
    return MemoryGlobalAvailabilitySlotStore(
        mutableMapOf<String, GlobalAvailabilitySlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAvailabilitySlotStore(
    var inner: GlobalAvailabilitySlotStore,
    var cache: TtlCache<GlobalAvailabilitySlot>,
    var hits: Int,
    var misses: Int,
) : GlobalAvailabilitySlotStore {
    override fun find(code: String): GlobalAvailabilitySlot? {
        val hit = (cache.get(code) as GlobalAvailabilitySlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAvailabilitySlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAvailabilitySlot> = inner.list()
}

fun newCachedGlobalAvailabilitySlotStore(inner: GlobalAvailabilitySlotStore, ttl: Int, clock: Clock): GlobalAvailabilitySlotStore {
    return CachedGlobalAvailabilitySlotStore(
        inner = inner,
        cache = newTtlCache<GlobalAvailabilitySlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global availability slots. Empty fields match all. */
data class GlobalAvailabilitySlotQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAvailabilitySlot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAvailabilitySlot>): MutableList<GlobalAvailabilitySlot> {
        val matched = filter(items) { candidate: GlobalAvailabilitySlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAvailabilitySlots(store: GlobalAvailabilitySlotStore, query: GlobalAvailabilitySlotQuery): MutableList<GlobalAvailabilitySlot> {
    return query.apply(store.list())
}
