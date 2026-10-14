package com.acme.transport.customs.broker.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped broker slots by code. */
interface CappedBrokerSlotStore {
    fun find(code: String): CappedBrokerSlot?
    fun save(item: CappedBrokerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBrokerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBrokerSlotStore(
    var items: MutableMap<String, CappedBrokerSlot>,
    var order: MutableList<String>,
) : CappedBrokerSlotStore {
    override fun find(code: String): CappedBrokerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBrokerSlot) {
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

    override fun list(): MutableList<CappedBrokerSlot> {
        val out = mutableListOf<CappedBrokerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBrokerSlotStore(): CappedBrokerSlotStore {
    return MemoryCappedBrokerSlotStore(mutableMapOf<String, CappedBrokerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBrokerSlotStore(
    var inner: CappedBrokerSlotStore,
    var cache: TtlCache<CappedBrokerSlot>,
    var hits: Int,
    var misses: Int,
) : CappedBrokerSlotStore {
    override fun find(code: String): CappedBrokerSlot? {
        val hit = (cache.get(code) as CappedBrokerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBrokerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBrokerSlot> = inner.list()
}

fun newCachedCappedBrokerSlotStore(inner: CappedBrokerSlotStore, ttl: Int, clock: Clock): CappedBrokerSlotStore {
    return CachedCappedBrokerSlotStore(
        inner = inner,
        cache = newTtlCache<CappedBrokerSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped broker slots. Empty fields match all. */
data class CappedBrokerSlotQuery(
    var depot: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBrokerSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedBrokerSlot>): MutableList<CappedBrokerSlot> {
        val matched = filter(items) { candidate: CappedBrokerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBrokerSlots(store: CappedBrokerSlotStore, query: CappedBrokerSlotQuery): MutableList<CappedBrokerSlot> {
    return query.apply(store.list())
}
