package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident slots by code. */
interface IncidentSlotStore {
    fun find(code: String): IncidentSlot?
    fun save(item: IncidentSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentSlotStore(
    var items: MutableMap<String, IncidentSlot>,
    var order: MutableList<String>,
) : IncidentSlotStore {
    override fun find(code: String): IncidentSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentSlot) {
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

    override fun list(): MutableList<IncidentSlot> {
        val out = mutableListOf<IncidentSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentSlotStore(): IncidentSlotStore {
    return MemoryIncidentSlotStore(mutableMapOf<String, IncidentSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentSlotStore(
    var inner: IncidentSlotStore,
    var cache: TtlCache<IncidentSlot>,
    var hits: Int,
    var misses: Int,
) : IncidentSlotStore {
    override fun find(code: String): IncidentSlot? {
        val hit = (cache.get(code) as IncidentSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentSlot> = inner.list()
}

fun newCachedIncidentSlotStore(inner: IncidentSlotStore, ttl: Int, clock: Clock): IncidentSlotStore {
    return CachedIncidentSlotStore(
        inner = inner,
        cache = newTtlCache<IncidentSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident slots. Empty fields match all. */
data class IncidentSlotQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentSlot>): MutableList<IncidentSlot> {
        val matched = filter(items) { candidate: IncidentSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentSlots(store: IncidentSlotStore, query: IncidentSlotQuery): MutableList<IncidentSlot> {
    return query.apply(store.list())
}
