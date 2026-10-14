package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching assignments by code. */
interface CoachingAssignmentStore {
    fun find(code: String): CoachingAssignment?
    fun save(item: CoachingAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingAssignmentStore(
    var items: MutableMap<String, CoachingAssignment>,
    var order: MutableList<String>,
) : CoachingAssignmentStore {
    override fun find(code: String): CoachingAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingAssignment) {
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

    override fun list(): MutableList<CoachingAssignment> {
        val out = mutableListOf<CoachingAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingAssignmentStore(): CoachingAssignmentStore {
    return MemoryCoachingAssignmentStore(mutableMapOf<String, CoachingAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingAssignmentStore(
    var inner: CoachingAssignmentStore,
    var cache: TtlCache<CoachingAssignment>,
    var hits: Int,
    var misses: Int,
) : CoachingAssignmentStore {
    override fun find(code: String): CoachingAssignment? {
        val hit = (cache.get(code) as CoachingAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingAssignment> = inner.list()
}

fun newCachedCoachingAssignmentStore(inner: CoachingAssignmentStore, ttl: Int, clock: Clock): CoachingAssignmentStore {
    return CachedCoachingAssignmentStore(
        inner = inner,
        cache = newTtlCache<CoachingAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching assignments. Empty fields match all. */
data class CoachingAssignmentQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingAssignment>): MutableList<CoachingAssignment> {
        val matched = filter(items) { candidate: CoachingAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingAssignments(store: CoachingAssignmentStore, query: CoachingAssignmentQuery): MutableList<CoachingAssignment> {
    return query.apply(store.list())
}
