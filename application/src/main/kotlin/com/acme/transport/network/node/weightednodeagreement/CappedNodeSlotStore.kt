package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped node slots by code. */
interface CappedNodeSlotStore {
    fun find(code: String): CappedNodeSlot?
    fun save(item: CappedNodeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNodeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNodeSlotStore(
    var items: MutableMap<String, CappedNodeSlot>,
    var order: MutableList<String>,
) : CappedNodeSlotStore {
    override fun find(code: String): CappedNodeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNodeSlot) {
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

    override fun list(): MutableList<CappedNodeSlot> {
        val out = mutableListOf<CappedNodeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNodeSlotStore(): CappedNodeSlotStore {
    return MemoryCappedNodeSlotStore(mutableMapOf<String, CappedNodeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNodeSlotStore(
    var inner: CappedNodeSlotStore,
    var cache: TtlCache<CappedNodeSlot>,
    var hits: Int,
    var misses: Int,
) : CappedNodeSlotStore {
    override fun find(code: String): CappedNodeSlot? {
        val hit = (cache.get(code) as CappedNodeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNodeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNodeSlot> = inner.list()
}

fun newCachedCappedNodeSlotStore(inner: CappedNodeSlotStore, ttl: Int, clock: Clock): CappedNodeSlotStore {
    return CachedCappedNodeSlotStore(
        inner = inner,
        cache = newTtlCache<CappedNodeSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped node slots. Empty fields match all. */
data class CappedNodeSlotQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNodeSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CappedNodeSlot>): MutableList<CappedNodeSlot> {
        val matched = filter(items) { candidate: CappedNodeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNodeSlots(store: CappedNodeSlotStore, query: CappedNodeSlotQuery): MutableList<CappedNodeSlot> {
    return query.apply(store.list())
}
