package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break assignments by code. */
interface RestBreakAssignmentStore {
    fun find(code: String): RestBreakAssignment?
    fun save(item: RestBreakAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakAssignmentStore(
    var items: MutableMap<String, RestBreakAssignment>,
    var order: MutableList<String>,
) : RestBreakAssignmentStore {
    override fun find(code: String): RestBreakAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakAssignment) {
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

    override fun list(): MutableList<RestBreakAssignment> {
        val out = mutableListOf<RestBreakAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakAssignmentStore(): RestBreakAssignmentStore {
    return MemoryRestBreakAssignmentStore(mutableMapOf<String, RestBreakAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakAssignmentStore(
    var inner: RestBreakAssignmentStore,
    var cache: TtlCache<RestBreakAssignment>,
    var hits: Int,
    var misses: Int,
) : RestBreakAssignmentStore {
    override fun find(code: String): RestBreakAssignment? {
        val hit = (cache.get(code) as RestBreakAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakAssignment> = inner.list()
}

fun newCachedRestBreakAssignmentStore(inner: RestBreakAssignmentStore, ttl: Int, clock: Clock): RestBreakAssignmentStore {
    return CachedRestBreakAssignmentStore(
        inner = inner,
        cache = newTtlCache<RestBreakAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break assignments. Empty fields match all. */
data class RestBreakAssignmentQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RestBreakAssignment>): MutableList<RestBreakAssignment> {
        val matched = filter(items) { candidate: RestBreakAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakAssignments(store: RestBreakAssignmentStore, query: RestBreakAssignmentQuery): MutableList<RestBreakAssignment> {
    return query.apply(store.list())
}
