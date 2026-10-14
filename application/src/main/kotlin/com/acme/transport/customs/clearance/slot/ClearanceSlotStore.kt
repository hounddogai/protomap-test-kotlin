package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance slots by code. */
interface ClearanceSlotStore {
    fun find(code: String): ClearanceSlot?
    fun save(item: ClearanceSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceSlotStore(
    var items: MutableMap<String, ClearanceSlot>,
    var order: MutableList<String>,
) : ClearanceSlotStore {
    override fun find(code: String): ClearanceSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceSlot) {
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

    override fun list(): MutableList<ClearanceSlot> {
        val out = mutableListOf<ClearanceSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceSlotStore(): ClearanceSlotStore {
    return MemoryClearanceSlotStore(mutableMapOf<String, ClearanceSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceSlotStore(
    var inner: ClearanceSlotStore,
    var cache: TtlCache<ClearanceSlot>,
    var hits: Int,
    var misses: Int,
) : ClearanceSlotStore {
    override fun find(code: String): ClearanceSlot? {
        val hit = (cache.get(code) as ClearanceSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceSlot> = inner.list()
}

fun newCachedClearanceSlotStore(inner: ClearanceSlotStore, ttl: Int, clock: Clock): ClearanceSlotStore {
    return CachedClearanceSlotStore(
        inner = inner,
        cache = newTtlCache<ClearanceSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance slots. Empty fields match all. */
data class ClearanceSlotQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceSlot>): MutableList<ClearanceSlot> {
        val matched = filter(items) { candidate: ClearanceSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceSlots(store: ClearanceSlotStore, query: ClearanceSlotQuery): MutableList<ClearanceSlot> {
    return query.apply(store.list())
}
