package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door assignments by code. */
interface ScheduledDockDoorAssignmentStore {
    fun find(code: String): ScheduledDockDoorAssignment?
    fun save(item: ScheduledDockDoorAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorAssignmentStore(
    var items: MutableMap<String, ScheduledDockDoorAssignment>,
    var order: MutableList<String>,
) : ScheduledDockDoorAssignmentStore {
    override fun find(code: String): ScheduledDockDoorAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorAssignment) {
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

    override fun list(): MutableList<ScheduledDockDoorAssignment> {
        val out = mutableListOf<ScheduledDockDoorAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorAssignmentStore(): ScheduledDockDoorAssignmentStore {
    return MemoryScheduledDockDoorAssignmentStore(
        mutableMapOf<String, ScheduledDockDoorAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorAssignmentStore(
    var inner: ScheduledDockDoorAssignmentStore,
    var cache: TtlCache<ScheduledDockDoorAssignment>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorAssignmentStore {
    override fun find(code: String): ScheduledDockDoorAssignment? {
        val hit = (cache.get(code) as ScheduledDockDoorAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorAssignment> = inner.list()
}

fun newCachedScheduledDockDoorAssignmentStore(inner: ScheduledDockDoorAssignmentStore, ttl: Int, clock: Clock): ScheduledDockDoorAssignmentStore {
    return CachedScheduledDockDoorAssignmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door assignments. Empty fields match all. */
data class ScheduledDockDoorAssignmentQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDockDoorAssignment>): MutableList<ScheduledDockDoorAssignment> {
        val matched = filter(items) { candidate: ScheduledDockDoorAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorAssignments(store: ScheduledDockDoorAssignmentStore, query: ScheduledDockDoorAssignmentQuery): MutableList<ScheduledDockDoorAssignment> {
    return query.apply(store.list())
}
