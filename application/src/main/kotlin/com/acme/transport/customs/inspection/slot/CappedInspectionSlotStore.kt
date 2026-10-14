package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection slots by code. */
interface CappedInspectionSlotStore {
    fun find(code: String): CappedInspectionSlot?
    fun save(item: CappedInspectionSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionSlotStore(
    var items: MutableMap<String, CappedInspectionSlot>,
    var order: MutableList<String>,
) : CappedInspectionSlotStore {
    override fun find(code: String): CappedInspectionSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionSlot) {
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

    override fun list(): MutableList<CappedInspectionSlot> {
        val out = mutableListOf<CappedInspectionSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionSlotStore(): CappedInspectionSlotStore {
    return MemoryCappedInspectionSlotStore(
        mutableMapOf<String, CappedInspectionSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionSlotStore(
    var inner: CappedInspectionSlotStore,
    var cache: TtlCache<CappedInspectionSlot>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionSlotStore {
    override fun find(code: String): CappedInspectionSlot? {
        val hit = (cache.get(code) as CappedInspectionSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionSlot> = inner.list()
}

fun newCachedCappedInspectionSlotStore(inner: CappedInspectionSlotStore, ttl: Int, clock: Clock): CappedInspectionSlotStore {
    return CachedCappedInspectionSlotStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection slots. Empty fields match all. */
data class CappedInspectionSlotQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedInspectionSlot>): MutableList<CappedInspectionSlot> {
        val matched = filter(items) { candidate: CappedInspectionSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionSlots(store: CappedInspectionSlotStore, query: CappedInspectionSlotQuery): MutableList<CappedInspectionSlot> {
    return query.apply(store.list())
}
