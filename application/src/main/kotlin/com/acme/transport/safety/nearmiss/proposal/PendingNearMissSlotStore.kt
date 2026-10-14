package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending near miss slots by code. */
interface PendingNearMissSlotStore {
    fun find(code: String): PendingNearMissSlot?
    fun save(item: PendingNearMissSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNearMissSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNearMissSlotStore(
    var items: MutableMap<String, PendingNearMissSlot>,
    var order: MutableList<String>,
) : PendingNearMissSlotStore {
    override fun find(code: String): PendingNearMissSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNearMissSlot) {
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

    override fun list(): MutableList<PendingNearMissSlot> {
        val out = mutableListOf<PendingNearMissSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNearMissSlotStore(): PendingNearMissSlotStore {
    return MemoryPendingNearMissSlotStore(mutableMapOf<String, PendingNearMissSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNearMissSlotStore(
    var inner: PendingNearMissSlotStore,
    var cache: TtlCache<PendingNearMissSlot>,
    var hits: Int,
    var misses: Int,
) : PendingNearMissSlotStore {
    override fun find(code: String): PendingNearMissSlot? {
        val hit = (cache.get(code) as PendingNearMissSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNearMissSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNearMissSlot> = inner.list()
}

fun newCachedPendingNearMissSlotStore(inner: PendingNearMissSlotStore, ttl: Int, clock: Clock): PendingNearMissSlotStore {
    return CachedPendingNearMissSlotStore(
        inner = inner,
        cache = newTtlCache<PendingNearMissSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending near miss slots. Empty fields match all. */
data class PendingNearMissSlotQuery(
    var unit: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNearMissSlot): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNearMissSlot>): MutableList<PendingNearMissSlot> {
        val matched = filter(items) { candidate: PendingNearMissSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNearMissSlots(store: PendingNearMissSlotStore, query: PendingNearMissSlotQuery): MutableList<PendingNearMissSlot> {
    return query.apply(store.list())
}
