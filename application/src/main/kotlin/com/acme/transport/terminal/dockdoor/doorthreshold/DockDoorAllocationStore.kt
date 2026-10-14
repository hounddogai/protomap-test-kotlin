package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door allocations by code. */
interface DockDoorAllocationStore {
    fun find(code: String): DockDoorAllocation?
    fun save(item: DockDoorAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorAllocationStore(
    var items: MutableMap<String, DockDoorAllocation>,
    var order: MutableList<String>,
) : DockDoorAllocationStore {
    override fun find(code: String): DockDoorAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorAllocation) {
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

    override fun list(): MutableList<DockDoorAllocation> {
        val out = mutableListOf<DockDoorAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorAllocationStore(): DockDoorAllocationStore {
    return MemoryDockDoorAllocationStore(mutableMapOf<String, DockDoorAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorAllocationStore(
    var inner: DockDoorAllocationStore,
    var cache: TtlCache<DockDoorAllocation>,
    var hits: Int,
    var misses: Int,
) : DockDoorAllocationStore {
    override fun find(code: String): DockDoorAllocation? {
        val hit = (cache.get(code) as DockDoorAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorAllocation> = inner.list()
}

fun newCachedDockDoorAllocationStore(inner: DockDoorAllocationStore, ttl: Int, clock: Clock): DockDoorAllocationStore {
    return CachedDockDoorAllocationStore(
        inner = inner,
        cache = newTtlCache<DockDoorAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door allocations. Empty fields match all. */
data class DockDoorAllocationQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorAllocation>): MutableList<DockDoorAllocation> {
        val matched = filter(items) { candidate: DockDoorAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorAllocations(store: DockDoorAllocationStore, query: DockDoorAllocationQuery): MutableList<DockDoorAllocation> {
    return query.apply(store.list())
}
