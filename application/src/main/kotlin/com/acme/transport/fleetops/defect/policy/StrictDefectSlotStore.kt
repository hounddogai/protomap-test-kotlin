package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict defect slots by code. */
interface StrictDefectSlotStore {
    fun find(code: String): StrictDefectSlot?
    fun save(item: StrictDefectSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDefectSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDefectSlotStore(
    var items: MutableMap<String, StrictDefectSlot>,
    var order: MutableList<String>,
) : StrictDefectSlotStore {
    override fun find(code: String): StrictDefectSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDefectSlot) {
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

    override fun list(): MutableList<StrictDefectSlot> {
        val out = mutableListOf<StrictDefectSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDefectSlotStore(): StrictDefectSlotStore {
    return MemoryStrictDefectSlotStore(mutableMapOf<String, StrictDefectSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDefectSlotStore(
    var inner: StrictDefectSlotStore,
    var cache: TtlCache<StrictDefectSlot>,
    var hits: Int,
    var misses: Int,
) : StrictDefectSlotStore {
    override fun find(code: String): StrictDefectSlot? {
        val hit = (cache.get(code) as StrictDefectSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDefectSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDefectSlot> = inner.list()
}

fun newCachedStrictDefectSlotStore(inner: StrictDefectSlotStore, ttl: Int, clock: Clock): StrictDefectSlotStore {
    return CachedStrictDefectSlotStore(
        inner = inner,
        cache = newTtlCache<StrictDefectSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict defect slots. Empty fields match all. */
data class StrictDefectSlotQuery(
    var locale: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDefectSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<StrictDefectSlot>): MutableList<StrictDefectSlot> {
        val matched = filter(items) { candidate: StrictDefectSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDefectSlots(store: StrictDefectSlotStore, query: StrictDefectSlotQuery): MutableList<StrictDefectSlot> {
    return query.apply(store.list())
}
