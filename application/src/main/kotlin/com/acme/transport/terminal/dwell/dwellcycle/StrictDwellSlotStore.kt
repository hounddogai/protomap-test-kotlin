package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dwell slots by code. */
interface StrictDwellSlotStore {
    fun find(code: String): StrictDwellSlot?
    fun save(item: StrictDwellSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDwellSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDwellSlotStore(
    var items: MutableMap<String, StrictDwellSlot>,
    var order: MutableList<String>,
) : StrictDwellSlotStore {
    override fun find(code: String): StrictDwellSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDwellSlot) {
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

    override fun list(): MutableList<StrictDwellSlot> {
        val out = mutableListOf<StrictDwellSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDwellSlotStore(): StrictDwellSlotStore {
    return MemoryStrictDwellSlotStore(mutableMapOf<String, StrictDwellSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDwellSlotStore(
    var inner: StrictDwellSlotStore,
    var cache: TtlCache<StrictDwellSlot>,
    var hits: Int,
    var misses: Int,
) : StrictDwellSlotStore {
    override fun find(code: String): StrictDwellSlot? {
        val hit = (cache.get(code) as StrictDwellSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDwellSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDwellSlot> = inner.list()
}

fun newCachedStrictDwellSlotStore(inner: StrictDwellSlotStore, ttl: Int, clock: Clock): StrictDwellSlotStore {
    return CachedStrictDwellSlotStore(
        inner = inner,
        cache = newTtlCache<StrictDwellSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dwell slots. Empty fields match all. */
data class StrictDwellSlotQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDwellSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<StrictDwellSlot>): MutableList<StrictDwellSlot> {
        val matched = filter(items) { candidate: StrictDwellSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDwellSlots(store: StrictDwellSlotStore, query: StrictDwellSlotQuery): MutableList<StrictDwellSlot> {
    return query.apply(store.list())
}
