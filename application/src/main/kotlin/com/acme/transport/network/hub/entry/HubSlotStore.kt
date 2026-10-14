package com.acme.transport.network.hub.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub slots by code. */
interface HubSlotStore {
    fun find(code: String): HubSlot?
    fun save(item: HubSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubSlotStore(
    var items: MutableMap<String, HubSlot>,
    var order: MutableList<String>,
) : HubSlotStore {
    override fun find(code: String): HubSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubSlot) {
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

    override fun list(): MutableList<HubSlot> {
        val out = mutableListOf<HubSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubSlotStore(): HubSlotStore {
    return MemoryHubSlotStore(mutableMapOf<String, HubSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubSlotStore(
    var inner: HubSlotStore,
    var cache: TtlCache<HubSlot>,
    var hits: Int,
    var misses: Int,
) : HubSlotStore {
    override fun find(code: String): HubSlot? {
        val hit = (cache.get(code) as HubSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubSlot> = inner.list()
}

fun newCachedHubSlotStore(inner: HubSlotStore, ttl: Int, clock: Clock): HubSlotStore {
    return CachedHubSlotStore(
        inner = inner,
        cache = newTtlCache<HubSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub slots. Empty fields match all. */
data class HubSlotQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubSlot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<HubSlot>): MutableList<HubSlot> {
        val matched = filter(items) { candidate: HubSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubSlots(store: HubSlotStore, query: HubSlotQuery): MutableList<HubSlot> = query.apply(store.list())
