package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell assignments by code. */
interface DwellAssignmentStore {
    fun find(code: String): DwellAssignment?
    fun save(item: DwellAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellAssignmentStore(
    var items: MutableMap<String, DwellAssignment>,
    var order: MutableList<String>,
) : DwellAssignmentStore {
    override fun find(code: String): DwellAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellAssignment) {
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

    override fun list(): MutableList<DwellAssignment> {
        val out = mutableListOf<DwellAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellAssignmentStore(): DwellAssignmentStore {
    return MemoryDwellAssignmentStore(mutableMapOf<String, DwellAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellAssignmentStore(
    var inner: DwellAssignmentStore,
    var cache: TtlCache<DwellAssignment>,
    var hits: Int,
    var misses: Int,
) : DwellAssignmentStore {
    override fun find(code: String): DwellAssignment? {
        val hit = (cache.get(code) as DwellAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellAssignment> = inner.list()
}

fun newCachedDwellAssignmentStore(inner: DwellAssignmentStore, ttl: Int, clock: Clock): DwellAssignmentStore {
    return CachedDwellAssignmentStore(
        inner = inner,
        cache = newTtlCache<DwellAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell assignments. Empty fields match all. */
data class DwellAssignmentQuery(
    var depot: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellAssignment>): MutableList<DwellAssignment> {
        val matched = filter(items) { candidate: DwellAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellAssignments(store: DwellAssignmentStore, query: DwellAssignmentQuery): MutableList<DwellAssignment> {
    return query.apply(store.list())
}
