package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual dock door assignments by code. */
interface ManualDockDoorAssignmentStore {
    fun find(code: String): ManualDockDoorAssignment?
    fun save(item: ManualDockDoorAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDockDoorAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDockDoorAssignmentStore(
    var items: MutableMap<String, ManualDockDoorAssignment>,
    var order: MutableList<String>,
) : ManualDockDoorAssignmentStore {
    override fun find(code: String): ManualDockDoorAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDockDoorAssignment) {
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

    override fun list(): MutableList<ManualDockDoorAssignment> {
        val out = mutableListOf<ManualDockDoorAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDockDoorAssignmentStore(): ManualDockDoorAssignmentStore {
    return MemoryManualDockDoorAssignmentStore(
        mutableMapOf<String, ManualDockDoorAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDockDoorAssignmentStore(
    var inner: ManualDockDoorAssignmentStore,
    var cache: TtlCache<ManualDockDoorAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualDockDoorAssignmentStore {
    override fun find(code: String): ManualDockDoorAssignment? {
        val hit = (cache.get(code) as ManualDockDoorAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDockDoorAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDockDoorAssignment> = inner.list()
}

fun newCachedManualDockDoorAssignmentStore(inner: ManualDockDoorAssignmentStore, ttl: Int, clock: Clock): ManualDockDoorAssignmentStore {
    return CachedManualDockDoorAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualDockDoorAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dock door assignments. Empty fields match all. */
data class ManualDockDoorAssignmentQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDockDoorAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDockDoorAssignment>): MutableList<ManualDockDoorAssignment> {
        val matched = filter(items) { candidate: ManualDockDoorAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDockDoorAssignments(store: ManualDockDoorAssignmentStore, query: ManualDockDoorAssignmentQuery): MutableList<ManualDockDoorAssignment> {
    return query.apply(store.list())
}
