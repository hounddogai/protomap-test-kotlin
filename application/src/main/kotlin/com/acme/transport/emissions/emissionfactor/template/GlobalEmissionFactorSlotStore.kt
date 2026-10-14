package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global emission factor slots by code. */
interface GlobalEmissionFactorSlotStore {
    fun find(code: String): GlobalEmissionFactorSlot?
    fun save(item: GlobalEmissionFactorSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalEmissionFactorSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalEmissionFactorSlotStore(
    var items: MutableMap<String, GlobalEmissionFactorSlot>,
    var order: MutableList<String>,
) : GlobalEmissionFactorSlotStore {
    override fun find(code: String): GlobalEmissionFactorSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalEmissionFactorSlot) {
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

    override fun list(): MutableList<GlobalEmissionFactorSlot> {
        val out = mutableListOf<GlobalEmissionFactorSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalEmissionFactorSlotStore(): GlobalEmissionFactorSlotStore {
    return MemoryGlobalEmissionFactorSlotStore(
        mutableMapOf<String, GlobalEmissionFactorSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalEmissionFactorSlotStore(
    var inner: GlobalEmissionFactorSlotStore,
    var cache: TtlCache<GlobalEmissionFactorSlot>,
    var hits: Int,
    var misses: Int,
) : GlobalEmissionFactorSlotStore {
    override fun find(code: String): GlobalEmissionFactorSlot? {
        val hit = (cache.get(code) as GlobalEmissionFactorSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalEmissionFactorSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalEmissionFactorSlot> = inner.list()
}

fun newCachedGlobalEmissionFactorSlotStore(inner: GlobalEmissionFactorSlotStore, ttl: Int, clock: Clock): GlobalEmissionFactorSlotStore {
    return CachedGlobalEmissionFactorSlotStore(
        inner = inner,
        cache = newTtlCache<GlobalEmissionFactorSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global emission factor slots. Empty fields match all. */
data class GlobalEmissionFactorSlotQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalEmissionFactorSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalEmissionFactorSlot>): MutableList<GlobalEmissionFactorSlot> {
        val matched = filter(items) { candidate: GlobalEmissionFactorSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalEmissionFactorSlots(store: GlobalEmissionFactorSlotStore, query: GlobalEmissionFactorSlotQuery): MutableList<GlobalEmissionFactorSlot> {
    return query.apply(store.list())
}
