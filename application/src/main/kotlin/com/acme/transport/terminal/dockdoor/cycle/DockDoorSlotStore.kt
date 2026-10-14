package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door slots by code. */
interface DockDoorSlotStore {
    fun find(code: String): DockDoorSlot?
    fun save(item: DockDoorSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorSlotStore(
    var items: MutableMap<String, DockDoorSlot>,
    var order: MutableList<String>,
) : DockDoorSlotStore {
    override fun find(code: String): DockDoorSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorSlot) {
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

    override fun list(): MutableList<DockDoorSlot> {
        val out = mutableListOf<DockDoorSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorSlotStore(): DockDoorSlotStore {
    return MemoryDockDoorSlotStore(mutableMapOf<String, DockDoorSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorSlotStore(
    var inner: DockDoorSlotStore,
    var cache: TtlCache<DockDoorSlot>,
    var hits: Int,
    var misses: Int,
) : DockDoorSlotStore {
    override fun find(code: String): DockDoorSlot? {
        val hit = (cache.get(code) as DockDoorSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorSlot> = inner.list()
}

fun newCachedDockDoorSlotStore(inner: DockDoorSlotStore, ttl: Int, clock: Clock): DockDoorSlotStore {
    return CachedDockDoorSlotStore(
        inner = inner,
        cache = newTtlCache<DockDoorSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door slots. Empty fields match all. */
data class DockDoorSlotQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorSlot>): MutableList<DockDoorSlot> {
        val matched = filter(items) { candidate: DockDoorSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorSlots(store: DockDoorSlotStore, query: DockDoorSlotQuery): MutableList<DockDoorSlot> {
    return query.apply(store.list())
}
