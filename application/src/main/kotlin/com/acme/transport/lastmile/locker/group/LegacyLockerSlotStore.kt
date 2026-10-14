package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy locker slots by code. */
interface LegacyLockerSlotStore {
    fun find(code: String): LegacyLockerSlot?
    fun save(item: LegacyLockerSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLockerSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLockerSlotStore(
    var items: MutableMap<String, LegacyLockerSlot>,
    var order: MutableList<String>,
) : LegacyLockerSlotStore {
    override fun find(code: String): LegacyLockerSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLockerSlot) {
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

    override fun list(): MutableList<LegacyLockerSlot> {
        val out = mutableListOf<LegacyLockerSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLockerSlotStore(): LegacyLockerSlotStore {
    return MemoryLegacyLockerSlotStore(mutableMapOf<String, LegacyLockerSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLockerSlotStore(
    var inner: LegacyLockerSlotStore,
    var cache: TtlCache<LegacyLockerSlot>,
    var hits: Int,
    var misses: Int,
) : LegacyLockerSlotStore {
    override fun find(code: String): LegacyLockerSlot? {
        val hit = (cache.get(code) as LegacyLockerSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLockerSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLockerSlot> = inner.list()
}

fun newCachedLegacyLockerSlotStore(inner: LegacyLockerSlotStore, ttl: Int, clock: Clock): LegacyLockerSlotStore {
    return CachedLegacyLockerSlotStore(
        inner = inner,
        cache = newTtlCache<LegacyLockerSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy locker slots. Empty fields match all. */
data class LegacyLockerSlotQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLockerSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLockerSlot>): MutableList<LegacyLockerSlot> {
        val matched = filter(items) { candidate: LegacyLockerSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLockerSlots(store: LegacyLockerSlotStore, query: LegacyLockerSlotQuery): MutableList<LegacyLockerSlot> {
    return query.apply(store.list())
}
