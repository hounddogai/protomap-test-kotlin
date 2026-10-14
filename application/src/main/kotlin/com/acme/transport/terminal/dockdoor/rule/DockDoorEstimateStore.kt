package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door estimates by code. */
interface DockDoorEstimateStore {
    fun find(code: String): DockDoorEstimate?
    fun save(item: DockDoorEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorEstimateStore(
    var items: MutableMap<String, DockDoorEstimate>,
    var order: MutableList<String>,
) : DockDoorEstimateStore {
    override fun find(code: String): DockDoorEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorEstimate) {
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

    override fun list(): MutableList<DockDoorEstimate> {
        val out = mutableListOf<DockDoorEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorEstimateStore(): DockDoorEstimateStore {
    return MemoryDockDoorEstimateStore(mutableMapOf<String, DockDoorEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorEstimateStore(
    var inner: DockDoorEstimateStore,
    var cache: TtlCache<DockDoorEstimate>,
    var hits: Int,
    var misses: Int,
) : DockDoorEstimateStore {
    override fun find(code: String): DockDoorEstimate? {
        val hit = (cache.get(code) as DockDoorEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorEstimate> = inner.list()
}

fun newCachedDockDoorEstimateStore(inner: DockDoorEstimateStore, ttl: Int, clock: Clock): DockDoorEstimateStore {
    return CachedDockDoorEstimateStore(
        inner = inner,
        cache = newTtlCache<DockDoorEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door estimates. Empty fields match all. */
data class DockDoorEstimateQuery(
    var lane: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorEstimate>): MutableList<DockDoorEstimate> {
        val matched = filter(items) { candidate: DockDoorEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorEstimates(store: DockDoorEstimateStore, query: DockDoorEstimateQuery): MutableList<DockDoorEstimate> {
    return query.apply(store.list())
}
