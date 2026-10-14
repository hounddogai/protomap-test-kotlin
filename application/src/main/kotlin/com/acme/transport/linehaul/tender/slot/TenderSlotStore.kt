package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender slots by code. */
interface TenderSlotStore {
    fun find(code: String): TenderSlot?
    fun save(item: TenderSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderSlotStore(
    var items: MutableMap<String, TenderSlot>,
    var order: MutableList<String>,
) : TenderSlotStore {
    override fun find(code: String): TenderSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderSlot) {
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

    override fun list(): MutableList<TenderSlot> {
        val out = mutableListOf<TenderSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderSlotStore(): TenderSlotStore {
    return MemoryTenderSlotStore(mutableMapOf<String, TenderSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderSlotStore(
    var inner: TenderSlotStore,
    var cache: TtlCache<TenderSlot>,
    var hits: Int,
    var misses: Int,
) : TenderSlotStore {
    override fun find(code: String): TenderSlot? {
        val hit = (cache.get(code) as TenderSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderSlot> = inner.list()
}

fun newCachedTenderSlotStore(inner: TenderSlotStore, ttl: Int, clock: Clock): TenderSlotStore {
    return CachedTenderSlotStore(
        inner = inner,
        cache = newTtlCache<TenderSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender slots. Empty fields match all. */
data class TenderSlotQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<TenderSlot>): MutableList<TenderSlot> {
        val matched = filter(items) { candidate: TenderSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderSlots(store: TenderSlotStore, query: TenderSlotQuery): MutableList<TenderSlot> {
    return query.apply(store.list())
}
