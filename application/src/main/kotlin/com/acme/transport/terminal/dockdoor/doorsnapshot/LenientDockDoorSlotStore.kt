package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dock door slots by code. */
interface LenientDockDoorSlotStore {
    fun find(code: String): LenientDockDoorSlot?
    fun save(item: LenientDockDoorSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDockDoorSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDockDoorSlotStore(
    var items: MutableMap<String, LenientDockDoorSlot>,
    var order: MutableList<String>,
) : LenientDockDoorSlotStore {
    override fun find(code: String): LenientDockDoorSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDockDoorSlot) {
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

    override fun list(): MutableList<LenientDockDoorSlot> {
        val out = mutableListOf<LenientDockDoorSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDockDoorSlotStore(): LenientDockDoorSlotStore {
    return MemoryLenientDockDoorSlotStore(mutableMapOf<String, LenientDockDoorSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDockDoorSlotStore(
    var inner: LenientDockDoorSlotStore,
    var cache: TtlCache<LenientDockDoorSlot>,
    var hits: Int,
    var misses: Int,
) : LenientDockDoorSlotStore {
    override fun find(code: String): LenientDockDoorSlot? {
        val hit = (cache.get(code) as LenientDockDoorSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDockDoorSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDockDoorSlot> = inner.list()
}

fun newCachedLenientDockDoorSlotStore(inner: LenientDockDoorSlotStore, ttl: Int, clock: Clock): LenientDockDoorSlotStore {
    return CachedLenientDockDoorSlotStore(
        inner = inner,
        cache = newTtlCache<LenientDockDoorSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dock door slots. Empty fields match all. */
data class LenientDockDoorSlotQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDockDoorSlot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDockDoorSlot>): MutableList<LenientDockDoorSlot> {
        val matched = filter(items) { candidate: LenientDockDoorSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDockDoorSlots(store: LenientDockDoorSlotStore, query: LenientDockDoorSlotQuery): MutableList<LenientDockDoorSlot> {
    return query.apply(store.list())
}
