package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss assignments by code. */
interface NearMissAssignmentStore {
    fun find(code: String): NearMissAssignment?
    fun save(item: NearMissAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissAssignmentStore(
    var items: MutableMap<String, NearMissAssignment>,
    var order: MutableList<String>,
) : NearMissAssignmentStore {
    override fun find(code: String): NearMissAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissAssignment) {
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

    override fun list(): MutableList<NearMissAssignment> {
        val out = mutableListOf<NearMissAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissAssignmentStore(): NearMissAssignmentStore {
    return MemoryNearMissAssignmentStore(mutableMapOf<String, NearMissAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissAssignmentStore(
    var inner: NearMissAssignmentStore,
    var cache: TtlCache<NearMissAssignment>,
    var hits: Int,
    var misses: Int,
) : NearMissAssignmentStore {
    override fun find(code: String): NearMissAssignment? {
        val hit = (cache.get(code) as NearMissAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissAssignment> = inner.list()
}

fun newCachedNearMissAssignmentStore(inner: NearMissAssignmentStore, ttl: Int, clock: Clock): NearMissAssignmentStore {
    return CachedNearMissAssignmentStore(
        inner = inner,
        cache = newTtlCache<NearMissAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss assignments. Empty fields match all. */
data class NearMissAssignmentQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissAssignment>): MutableList<NearMissAssignment> {
        val matched = filter(items) { candidate: NearMissAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissAssignments(store: NearMissAssignmentStore, query: NearMissAssignmentQuery): MutableList<NearMissAssignment> {
    return query.apply(store.list())
}
