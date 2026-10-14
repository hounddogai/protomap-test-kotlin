package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered idling slots by code. */
interface TieredIdlingSlotStore {
    fun find(code: String): TieredIdlingSlot?
    fun save(item: TieredIdlingSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIdlingSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIdlingSlotStore(
    var items: MutableMap<String, TieredIdlingSlot>,
    var order: MutableList<String>,
) : TieredIdlingSlotStore {
    override fun find(code: String): TieredIdlingSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIdlingSlot) {
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

    override fun list(): MutableList<TieredIdlingSlot> {
        val out = mutableListOf<TieredIdlingSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIdlingSlotStore(): TieredIdlingSlotStore {
    return MemoryTieredIdlingSlotStore(mutableMapOf<String, TieredIdlingSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIdlingSlotStore(
    var inner: TieredIdlingSlotStore,
    var cache: TtlCache<TieredIdlingSlot>,
    var hits: Int,
    var misses: Int,
) : TieredIdlingSlotStore {
    override fun find(code: String): TieredIdlingSlot? {
        val hit = (cache.get(code) as TieredIdlingSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIdlingSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIdlingSlot> = inner.list()
}

fun newCachedTieredIdlingSlotStore(inner: TieredIdlingSlotStore, ttl: Int, clock: Clock): TieredIdlingSlotStore {
    return CachedTieredIdlingSlotStore(
        inner = inner,
        cache = newTtlCache<TieredIdlingSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered idling slots. Empty fields match all. */
data class TieredIdlingSlotQuery(
    var unit: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIdlingSlot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<TieredIdlingSlot>): MutableList<TieredIdlingSlot> {
        val matched = filter(items) { candidate: TieredIdlingSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIdlingSlots(store: TieredIdlingSlotStore, query: TieredIdlingSlotQuery): MutableList<TieredIdlingSlot> {
    return query.apply(store.list())
}
