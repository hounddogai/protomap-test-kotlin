package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect slots by code. */
interface DefectSlotStore {
    fun find(code: String): DefectSlot?
    fun save(item: DefectSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectSlotStore(
    var items: MutableMap<String, DefectSlot>,
    var order: MutableList<String>,
) : DefectSlotStore {
    override fun find(code: String): DefectSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectSlot) {
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

    override fun list(): MutableList<DefectSlot> {
        val out = mutableListOf<DefectSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectSlotStore(): DefectSlotStore {
    return MemoryDefectSlotStore(mutableMapOf<String, DefectSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectSlotStore(
    var inner: DefectSlotStore,
    var cache: TtlCache<DefectSlot>,
    var hits: Int,
    var misses: Int,
) : DefectSlotStore {
    override fun find(code: String): DefectSlot? {
        val hit = (cache.get(code) as DefectSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectSlot> = inner.list()
}

fun newCachedDefectSlotStore(inner: DefectSlotStore, ttl: Int, clock: Clock): DefectSlotStore {
    return CachedDefectSlotStore(
        inner = inner,
        cache = newTtlCache<DefectSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect slots. Empty fields match all. */
data class DefectSlotQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectSlot>): MutableList<DefectSlot> {
        val matched = filter(items) { candidate: DefectSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectSlots(store: DefectSlotStore, query: DefectSlotQuery): MutableList<DefectSlot> {
    return query.apply(store.list())
}
