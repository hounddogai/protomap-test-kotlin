package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending scorecard assignments by code. */
interface PendingScorecardAssignmentStore {
    fun find(code: String): PendingScorecardAssignment?
    fun save(item: PendingScorecardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingScorecardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingScorecardAssignmentStore(
    var items: MutableMap<String, PendingScorecardAssignment>,
    var order: MutableList<String>,
) : PendingScorecardAssignmentStore {
    override fun find(code: String): PendingScorecardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingScorecardAssignment) {
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

    override fun list(): MutableList<PendingScorecardAssignment> {
        val out = mutableListOf<PendingScorecardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingScorecardAssignmentStore(): PendingScorecardAssignmentStore {
    return MemoryPendingScorecardAssignmentStore(
        mutableMapOf<String, PendingScorecardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingScorecardAssignmentStore(
    var inner: PendingScorecardAssignmentStore,
    var cache: TtlCache<PendingScorecardAssignment>,
    var hits: Int,
    var misses: Int,
) : PendingScorecardAssignmentStore {
    override fun find(code: String): PendingScorecardAssignment? {
        val hit = (cache.get(code) as PendingScorecardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingScorecardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingScorecardAssignment> = inner.list()
}

fun newCachedPendingScorecardAssignmentStore(inner: PendingScorecardAssignmentStore, ttl: Int, clock: Clock): PendingScorecardAssignmentStore {
    return CachedPendingScorecardAssignmentStore(
        inner = inner,
        cache = newTtlCache<PendingScorecardAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending scorecard assignments. Empty fields match all. */
data class PendingScorecardAssignmentQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingScorecardAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingScorecardAssignment>): MutableList<PendingScorecardAssignment> {
        val matched = filter(items) { candidate: PendingScorecardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingScorecardAssignments(store: PendingScorecardAssignmentStore, query: PendingScorecardAssignmentQuery): MutableList<PendingScorecardAssignment> {
    return query.apply(store.list())
}
