package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient idling slots by code. */
interface LenientIdlingSlotStore {
    fun find(code: String): LenientIdlingSlot?
    fun save(item: LenientIdlingSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIdlingSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIdlingSlotStore(
    var items: MutableMap<String, LenientIdlingSlot>,
    var order: MutableList<String>,
) : LenientIdlingSlotStore {
    override fun find(code: String): LenientIdlingSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIdlingSlot) {
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

    override fun list(): MutableList<LenientIdlingSlot> {
        val out = mutableListOf<LenientIdlingSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIdlingSlotStore(): LenientIdlingSlotStore {
    return MemoryLenientIdlingSlotStore(mutableMapOf<String, LenientIdlingSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIdlingSlotStore(
    var inner: LenientIdlingSlotStore,
    var cache: TtlCache<LenientIdlingSlot>,
    var hits: Int,
    var misses: Int,
) : LenientIdlingSlotStore {
    override fun find(code: String): LenientIdlingSlot? {
        val hit = (cache.get(code) as LenientIdlingSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIdlingSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIdlingSlot> = inner.list()
}

fun newCachedLenientIdlingSlotStore(inner: LenientIdlingSlotStore, ttl: Int, clock: Clock): LenientIdlingSlotStore {
    return CachedLenientIdlingSlotStore(
        inner = inner,
        cache = newTtlCache<LenientIdlingSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient idling slots. Empty fields match all. */
data class LenientIdlingSlotQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIdlingSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LenientIdlingSlot>): MutableList<LenientIdlingSlot> {
        val matched = filter(items) { candidate: LenientIdlingSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIdlingSlots(store: LenientIdlingSlotStore, query: LenientIdlingSlotQuery): MutableList<LenientIdlingSlot> {
    return query.apply(store.list())
}
