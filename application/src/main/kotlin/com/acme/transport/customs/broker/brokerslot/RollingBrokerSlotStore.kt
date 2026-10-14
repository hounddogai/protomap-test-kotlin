package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling broker slots by code. */
interface RollingBrokerSlotStore {
    fun find(code: String): RollingBrokerSlot?
    fun save(item: RollingBrokerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBrokerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBrokerSlotStore(
    var items: MutableMap<String, RollingBrokerSlot>,
    var order: MutableList<String>,
) : RollingBrokerSlotStore {
    override fun find(code: String): RollingBrokerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBrokerSlot) {
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

    override fun list(): MutableList<RollingBrokerSlot> {
        val out = mutableListOf<RollingBrokerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBrokerSlotStore(): RollingBrokerSlotStore {
    return MemoryRollingBrokerSlotStore(mutableMapOf<String, RollingBrokerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBrokerSlotStore(
    var inner: RollingBrokerSlotStore,
    var cache: TtlCache<RollingBrokerSlot>,
    var hits: Int,
    var misses: Int,
) : RollingBrokerSlotStore {
    override fun find(code: String): RollingBrokerSlot? {
        val hit = (cache.get(code) as RollingBrokerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBrokerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBrokerSlot> = inner.list()
}

fun newCachedRollingBrokerSlotStore(inner: RollingBrokerSlotStore, ttl: Int, clock: Clock): RollingBrokerSlotStore {
    return CachedRollingBrokerSlotStore(
        inner = inner,
        cache = newTtlCache<RollingBrokerSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling broker slots. Empty fields match all. */
data class RollingBrokerSlotQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBrokerSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBrokerSlot>): MutableList<RollingBrokerSlot> {
        val matched = filter(items) { candidate: RollingBrokerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBrokerSlots(store: RollingBrokerSlotStore, query: RollingBrokerSlotQuery): MutableList<RollingBrokerSlot> {
    return query.apply(store.list())
}
