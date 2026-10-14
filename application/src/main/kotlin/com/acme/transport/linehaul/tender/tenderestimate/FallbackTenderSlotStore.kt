package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback tender slots by code. */
interface FallbackTenderSlotStore {
    fun find(code: String): FallbackTenderSlot?
    fun save(item: FallbackTenderSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTenderSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTenderSlotStore(
    var items: MutableMap<String, FallbackTenderSlot>,
    var order: MutableList<String>,
) : FallbackTenderSlotStore {
    override fun find(code: String): FallbackTenderSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTenderSlot) {
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

    override fun list(): MutableList<FallbackTenderSlot> {
        val out = mutableListOf<FallbackTenderSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTenderSlotStore(): FallbackTenderSlotStore {
    return MemoryFallbackTenderSlotStore(mutableMapOf<String, FallbackTenderSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTenderSlotStore(
    var inner: FallbackTenderSlotStore,
    var cache: TtlCache<FallbackTenderSlot>,
    var hits: Int,
    var misses: Int,
) : FallbackTenderSlotStore {
    override fun find(code: String): FallbackTenderSlot? {
        val hit = (cache.get(code) as FallbackTenderSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTenderSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTenderSlot> = inner.list()
}

fun newCachedFallbackTenderSlotStore(inner: FallbackTenderSlotStore, ttl: Int, clock: Clock): FallbackTenderSlotStore {
    return CachedFallbackTenderSlotStore(
        inner = inner,
        cache = newTtlCache<FallbackTenderSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tender slots. Empty fields match all. */
data class FallbackTenderSlotQuery(
    var locale: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTenderSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<FallbackTenderSlot>): MutableList<FallbackTenderSlot> {
        val matched = filter(items) { candidate: FallbackTenderSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTenderSlots(store: FallbackTenderSlotStore, query: FallbackTenderSlotQuery): MutableList<FallbackTenderSlot> {
    return query.apply(store.list())
}
