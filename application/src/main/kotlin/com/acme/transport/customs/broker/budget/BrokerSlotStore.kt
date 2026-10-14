package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker slots by code. */
interface BrokerSlotStore {
    fun find(code: String): BrokerSlot?
    fun save(item: BrokerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerSlotStore(
    var items: MutableMap<String, BrokerSlot>,
    var order: MutableList<String>,
) : BrokerSlotStore {
    override fun find(code: String): BrokerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerSlot) {
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

    override fun list(): MutableList<BrokerSlot> {
        val out = mutableListOf<BrokerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerSlotStore(): BrokerSlotStore {
    return MemoryBrokerSlotStore(mutableMapOf<String, BrokerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerSlotStore(
    var inner: BrokerSlotStore,
    var cache: TtlCache<BrokerSlot>,
    var hits: Int,
    var misses: Int,
) : BrokerSlotStore {
    override fun find(code: String): BrokerSlot? {
        val hit = (cache.get(code) as BrokerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerSlot> = inner.list()
}

fun newCachedBrokerSlotStore(inner: BrokerSlotStore, ttl: Int, clock: Clock): BrokerSlotStore {
    return CachedBrokerSlotStore(
        inner = inner,
        cache = newTtlCache<BrokerSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker slots. Empty fields match all. */
data class BrokerSlotQuery(
    var lane: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerSlot>): MutableList<BrokerSlot> {
        val matched = filter(items) { candidate: BrokerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerSlots(store: BrokerSlotStore, query: BrokerSlotQuery): MutableList<BrokerSlot> {
    return query.apply(store.list())
}
