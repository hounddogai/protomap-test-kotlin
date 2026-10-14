package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback incident slots by code. */
interface FallbackIncidentSlotStore {
    fun find(code: String): FallbackIncidentSlot?
    fun save(item: FallbackIncidentSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackIncidentSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackIncidentSlotStore(
    var items: MutableMap<String, FallbackIncidentSlot>,
    var order: MutableList<String>,
) : FallbackIncidentSlotStore {
    override fun find(code: String): FallbackIncidentSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackIncidentSlot) {
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

    override fun list(): MutableList<FallbackIncidentSlot> {
        val out = mutableListOf<FallbackIncidentSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackIncidentSlotStore(): FallbackIncidentSlotStore {
    return MemoryFallbackIncidentSlotStore(
        mutableMapOf<String, FallbackIncidentSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackIncidentSlotStore(
    var inner: FallbackIncidentSlotStore,
    var cache: TtlCache<FallbackIncidentSlot>,
    var hits: Int,
    var misses: Int,
) : FallbackIncidentSlotStore {
    override fun find(code: String): FallbackIncidentSlot? {
        val hit = (cache.get(code) as FallbackIncidentSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackIncidentSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackIncidentSlot> = inner.list()
}

fun newCachedFallbackIncidentSlotStore(inner: FallbackIncidentSlotStore, ttl: Int, clock: Clock): FallbackIncidentSlotStore {
    return CachedFallbackIncidentSlotStore(
        inner = inner,
        cache = newTtlCache<FallbackIncidentSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback incident slots. Empty fields match all. */
data class FallbackIncidentSlotQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackIncidentSlot): Boolean {
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

    fun apply(items: MutableList<FallbackIncidentSlot>): MutableList<FallbackIncidentSlot> {
        val matched = filter(items) { candidate: FallbackIncidentSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackIncidentSlots(store: FallbackIncidentSlotStore, query: FallbackIncidentSlotQuery): MutableList<FallbackIncidentSlot> {
    return query.apply(store.list())
}
