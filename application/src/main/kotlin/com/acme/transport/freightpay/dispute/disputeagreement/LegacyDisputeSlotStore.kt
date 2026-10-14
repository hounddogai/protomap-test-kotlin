package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy dispute slots by code. */
interface LegacyDisputeSlotStore {
    fun find(code: String): LegacyDisputeSlot?
    fun save(item: LegacyDisputeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDisputeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDisputeSlotStore(
    var items: MutableMap<String, LegacyDisputeSlot>,
    var order: MutableList<String>,
) : LegacyDisputeSlotStore {
    override fun find(code: String): LegacyDisputeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDisputeSlot) {
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

    override fun list(): MutableList<LegacyDisputeSlot> {
        val out = mutableListOf<LegacyDisputeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDisputeSlotStore(): LegacyDisputeSlotStore {
    return MemoryLegacyDisputeSlotStore(mutableMapOf<String, LegacyDisputeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDisputeSlotStore(
    var inner: LegacyDisputeSlotStore,
    var cache: TtlCache<LegacyDisputeSlot>,
    var hits: Int,
    var misses: Int,
) : LegacyDisputeSlotStore {
    override fun find(code: String): LegacyDisputeSlot? {
        val hit = (cache.get(code) as LegacyDisputeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDisputeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDisputeSlot> = inner.list()
}

fun newCachedLegacyDisputeSlotStore(inner: LegacyDisputeSlotStore, ttl: Int, clock: Clock): LegacyDisputeSlotStore {
    return CachedLegacyDisputeSlotStore(
        inner = inner,
        cache = newTtlCache<LegacyDisputeSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dispute slots. Empty fields match all. */
data class LegacyDisputeSlotQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDisputeSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDisputeSlot>): MutableList<LegacyDisputeSlot> {
        val matched = filter(items) { candidate: LegacyDisputeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDisputeSlots(store: LegacyDisputeSlotStore, query: LegacyDisputeSlotQuery): MutableList<LegacyDisputeSlot> {
    return query.apply(store.list())
}
