package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone slots by code. */
interface ZoneSlotStore {
    fun find(code: String): ZoneSlot?
    fun save(item: ZoneSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneSlotStore(
    var items: MutableMap<String, ZoneSlot>,
    var order: MutableList<String>,
) : ZoneSlotStore {
    override fun find(code: String): ZoneSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneSlot) {
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

    override fun list(): MutableList<ZoneSlot> {
        val out = mutableListOf<ZoneSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneSlotStore(): ZoneSlotStore {
    return MemoryZoneSlotStore(mutableMapOf<String, ZoneSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneSlotStore(
    var inner: ZoneSlotStore,
    var cache: TtlCache<ZoneSlot>,
    var hits: Int,
    var misses: Int,
) : ZoneSlotStore {
    override fun find(code: String): ZoneSlot? {
        val hit = (cache.get(code) as ZoneSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneSlot> = inner.list()
}

fun newCachedZoneSlotStore(inner: ZoneSlotStore, ttl: Int, clock: Clock): ZoneSlotStore {
    return CachedZoneSlotStore(
        inner = inner,
        cache = newTtlCache<ZoneSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone slots. Empty fields match all. */
data class ZoneSlotQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneSlot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneSlot>): MutableList<ZoneSlot> {
        val matched = filter(items) { candidate: ZoneSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneSlots(store: ZoneSlotStore, query: ZoneSlotQuery): MutableList<ZoneSlot> = query.apply(store.list())
