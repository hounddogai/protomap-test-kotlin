package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tire slots by code. */
interface CappedTireSlotStore {
    fun find(code: String): CappedTireSlot?
    fun save(item: CappedTireSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTireSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTireSlotStore(
    var items: MutableMap<String, CappedTireSlot>,
    var order: MutableList<String>,
) : CappedTireSlotStore {
    override fun find(code: String): CappedTireSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTireSlot) {
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

    override fun list(): MutableList<CappedTireSlot> {
        val out = mutableListOf<CappedTireSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTireSlotStore(): CappedTireSlotStore {
    return MemoryCappedTireSlotStore(mutableMapOf<String, CappedTireSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTireSlotStore(
    var inner: CappedTireSlotStore,
    var cache: TtlCache<CappedTireSlot>,
    var hits: Int,
    var misses: Int,
) : CappedTireSlotStore {
    override fun find(code: String): CappedTireSlot? {
        val hit = (cache.get(code) as CappedTireSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTireSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTireSlot> = inner.list()
}

fun newCachedCappedTireSlotStore(inner: CappedTireSlotStore, ttl: Int, clock: Clock): CappedTireSlotStore {
    return CachedCappedTireSlotStore(
        inner = inner,
        cache = newTtlCache<CappedTireSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tire slots. Empty fields match all. */
data class CappedTireSlotQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTireSlot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTireSlot>): MutableList<CappedTireSlot> {
        val matched = filter(items) { candidate: CappedTireSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTireSlots(store: CappedTireSlotStore, query: CappedTireSlotQuery): MutableList<CappedTireSlot> {
    return query.apply(store.list())
}
