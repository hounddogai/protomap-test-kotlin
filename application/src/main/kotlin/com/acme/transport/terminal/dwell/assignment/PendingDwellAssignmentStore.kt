package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending dwell assignments by code. */
interface PendingDwellAssignmentStore {
    fun find(code: String): PendingDwellAssignment?
    fun save(item: PendingDwellAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDwellAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDwellAssignmentStore(
    var items: MutableMap<String, PendingDwellAssignment>,
    var order: MutableList<String>,
) : PendingDwellAssignmentStore {
    override fun find(code: String): PendingDwellAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDwellAssignment) {
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

    override fun list(): MutableList<PendingDwellAssignment> {
        val out = mutableListOf<PendingDwellAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDwellAssignmentStore(): PendingDwellAssignmentStore {
    return MemoryPendingDwellAssignmentStore(
        mutableMapOf<String, PendingDwellAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDwellAssignmentStore(
    var inner: PendingDwellAssignmentStore,
    var cache: TtlCache<PendingDwellAssignment>,
    var hits: Int,
    var misses: Int,
) : PendingDwellAssignmentStore {
    override fun find(code: String): PendingDwellAssignment? {
        val hit = (cache.get(code) as PendingDwellAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDwellAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDwellAssignment> = inner.list()
}

fun newCachedPendingDwellAssignmentStore(inner: PendingDwellAssignmentStore, ttl: Int, clock: Clock): PendingDwellAssignmentStore {
    return CachedPendingDwellAssignmentStore(
        inner = inner,
        cache = newTtlCache<PendingDwellAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dwell assignments. Empty fields match all. */
data class PendingDwellAssignmentQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDwellAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDwellAssignment>): MutableList<PendingDwellAssignment> {
        val matched = filter(items) { candidate: PendingDwellAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDwellAssignments(store: PendingDwellAssignmentStore, query: PendingDwellAssignmentQuery): MutableList<PendingDwellAssignment> {
    return query.apply(store.list())
}
