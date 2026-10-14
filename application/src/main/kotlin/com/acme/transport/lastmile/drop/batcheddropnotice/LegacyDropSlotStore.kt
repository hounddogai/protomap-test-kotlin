package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy drop slots by code. */
interface LegacyDropSlotStore {
    fun find(code: String): LegacyDropSlot?
    fun save(item: LegacyDropSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDropSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDropSlotStore(
    var items: MutableMap<String, LegacyDropSlot>,
    var order: MutableList<String>,
) : LegacyDropSlotStore {
    override fun find(code: String): LegacyDropSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDropSlot) {
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

    override fun list(): MutableList<LegacyDropSlot> {
        val out = mutableListOf<LegacyDropSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDropSlotStore(): LegacyDropSlotStore {
    return MemoryLegacyDropSlotStore(mutableMapOf<String, LegacyDropSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDropSlotStore(
    var inner: LegacyDropSlotStore,
    var cache: TtlCache<LegacyDropSlot>,
    var hits: Int,
    var misses: Int,
) : LegacyDropSlotStore {
    override fun find(code: String): LegacyDropSlot? {
        val hit = (cache.get(code) as LegacyDropSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDropSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDropSlot> = inner.list()
}

fun newCachedLegacyDropSlotStore(inner: LegacyDropSlotStore, ttl: Int, clock: Clock): LegacyDropSlotStore {
    return CachedLegacyDropSlotStore(
        inner = inner,
        cache = newTtlCache<LegacyDropSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy drop slots. Empty fields match all. */
data class LegacyDropSlotQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDropSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDropSlot>): MutableList<LegacyDropSlot> {
        val matched = filter(items) { candidate: LegacyDropSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDropSlots(store: LegacyDropSlotStore, query: LegacyDropSlotQuery): MutableList<LegacyDropSlot> {
    return query.apply(store.list())
}
