package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door assignments by code. */
interface DockDoorAssignmentStore {
    fun find(code: String): DockDoorAssignment?
    fun save(item: DockDoorAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorAssignmentStore(
    var items: MutableMap<String, DockDoorAssignment>,
    var order: MutableList<String>,
) : DockDoorAssignmentStore {
    override fun find(code: String): DockDoorAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorAssignment) {
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

    override fun list(): MutableList<DockDoorAssignment> {
        val out = mutableListOf<DockDoorAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorAssignmentStore(): DockDoorAssignmentStore {
    return MemoryDockDoorAssignmentStore(mutableMapOf<String, DockDoorAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorAssignmentStore(
    var inner: DockDoorAssignmentStore,
    var cache: TtlCache<DockDoorAssignment>,
    var hits: Int,
    var misses: Int,
) : DockDoorAssignmentStore {
    override fun find(code: String): DockDoorAssignment? {
        val hit = (cache.get(code) as DockDoorAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorAssignment> = inner.list()
}

fun newCachedDockDoorAssignmentStore(inner: DockDoorAssignmentStore, ttl: Int, clock: Clock): DockDoorAssignmentStore {
    return CachedDockDoorAssignmentStore(
        inner = inner,
        cache = newTtlCache<DockDoorAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door assignments. Empty fields match all. */
data class DockDoorAssignmentQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorAssignment>): MutableList<DockDoorAssignment> {
        val matched = filter(items) { candidate: DockDoorAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorAssignments(store: DockDoorAssignmentStore, query: DockDoorAssignmentQuery): MutableList<DockDoorAssignment> {
    return query.apply(store.list())
}
