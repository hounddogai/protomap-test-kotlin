package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door rollups by code. */
interface DockDoorRollupStore {
    fun find(code: String): DockDoorRollup?
    fun save(item: DockDoorRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorRollupStore(
    var items: MutableMap<String, DockDoorRollup>,
    var order: MutableList<String>,
) : DockDoorRollupStore {
    override fun find(code: String): DockDoorRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorRollup) {
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

    override fun list(): MutableList<DockDoorRollup> {
        val out = mutableListOf<DockDoorRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorRollupStore(): DockDoorRollupStore {
    return MemoryDockDoorRollupStore(mutableMapOf<String, DockDoorRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorRollupStore(
    var inner: DockDoorRollupStore,
    var cache: TtlCache<DockDoorRollup>,
    var hits: Int,
    var misses: Int,
) : DockDoorRollupStore {
    override fun find(code: String): DockDoorRollup? {
        val hit = (cache.get(code) as DockDoorRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorRollup> = inner.list()
}

fun newCachedDockDoorRollupStore(inner: DockDoorRollupStore, ttl: Int, clock: Clock): DockDoorRollupStore {
    return CachedDockDoorRollupStore(
        inner = inner,
        cache = newTtlCache<DockDoorRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door rollups. Empty fields match all. */
data class DockDoorRollupQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorRollup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<DockDoorRollup>): MutableList<DockDoorRollup> {
        val matched = filter(items) { candidate: DockDoorRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorRollups(store: DockDoorRollupStore, query: DockDoorRollupQuery): MutableList<DockDoorRollup> {
    return query.apply(store.list())
}
