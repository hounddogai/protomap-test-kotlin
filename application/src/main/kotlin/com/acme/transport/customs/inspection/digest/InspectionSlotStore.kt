package com.acme.transport.customs.inspection.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection slots by code. */
interface InspectionSlotStore {
    fun find(code: String): InspectionSlot?
    fun save(item: InspectionSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionSlotStore(
    var items: MutableMap<String, InspectionSlot>,
    var order: MutableList<String>,
) : InspectionSlotStore {
    override fun find(code: String): InspectionSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionSlot) {
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

    override fun list(): MutableList<InspectionSlot> {
        val out = mutableListOf<InspectionSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionSlotStore(): InspectionSlotStore {
    return MemoryInspectionSlotStore(mutableMapOf<String, InspectionSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionSlotStore(
    var inner: InspectionSlotStore,
    var cache: TtlCache<InspectionSlot>,
    var hits: Int,
    var misses: Int,
) : InspectionSlotStore {
    override fun find(code: String): InspectionSlot? {
        val hit = (cache.get(code) as InspectionSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionSlot> = inner.list()
}

fun newCachedInspectionSlotStore(inner: InspectionSlotStore, ttl: Int, clock: Clock): InspectionSlotStore {
    return CachedInspectionSlotStore(
        inner = inner,
        cache = newTtlCache<InspectionSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection slots. Empty fields match all. */
data class InspectionSlotQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionSlot>): MutableList<InspectionSlot> {
        val matched = filter(items) { candidate: InspectionSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionSlots(store: InspectionSlotStore, query: InspectionSlotQuery): MutableList<InspectionSlot> {
    return query.apply(store.list())
}
