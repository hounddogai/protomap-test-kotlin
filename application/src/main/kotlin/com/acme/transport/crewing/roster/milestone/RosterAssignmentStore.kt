package com.acme.transport.crewing.roster.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster assignments by code. */
interface RosterAssignmentStore {
    fun find(code: String): RosterAssignment?
    fun save(item: RosterAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterAssignmentStore(
    var items: MutableMap<String, RosterAssignment>,
    var order: MutableList<String>,
) : RosterAssignmentStore {
    override fun find(code: String): RosterAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterAssignment) {
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

    override fun list(): MutableList<RosterAssignment> {
        val out = mutableListOf<RosterAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterAssignmentStore(): RosterAssignmentStore {
    return MemoryRosterAssignmentStore(mutableMapOf<String, RosterAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterAssignmentStore(
    var inner: RosterAssignmentStore,
    var cache: TtlCache<RosterAssignment>,
    var hits: Int,
    var misses: Int,
) : RosterAssignmentStore {
    override fun find(code: String): RosterAssignment? {
        val hit = (cache.get(code) as RosterAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterAssignment> = inner.list()
}

fun newCachedRosterAssignmentStore(inner: RosterAssignmentStore, ttl: Int, clock: Clock): RosterAssignmentStore {
    return CachedRosterAssignmentStore(
        inner = inner,
        cache = newTtlCache<RosterAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster assignments. Empty fields match all. */
data class RosterAssignmentQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RosterAssignment>): MutableList<RosterAssignment> {
        val matched = filter(items) { candidate: RosterAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterAssignments(store: RosterAssignmentStore, query: RosterAssignmentQuery): MutableList<RosterAssignment> {
    return query.apply(store.list())
}
