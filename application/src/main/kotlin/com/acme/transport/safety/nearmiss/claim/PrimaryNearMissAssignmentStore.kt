package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary near miss assignments by code. */
interface PrimaryNearMissAssignmentStore {
    fun find(code: String): PrimaryNearMissAssignment?
    fun save(item: PrimaryNearMissAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNearMissAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNearMissAssignmentStore(
    var items: MutableMap<String, PrimaryNearMissAssignment>,
    var order: MutableList<String>,
) : PrimaryNearMissAssignmentStore {
    override fun find(code: String): PrimaryNearMissAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNearMissAssignment) {
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

    override fun list(): MutableList<PrimaryNearMissAssignment> {
        val out = mutableListOf<PrimaryNearMissAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNearMissAssignmentStore(): PrimaryNearMissAssignmentStore {
    return MemoryPrimaryNearMissAssignmentStore(
        mutableMapOf<String, PrimaryNearMissAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNearMissAssignmentStore(
    var inner: PrimaryNearMissAssignmentStore,
    var cache: TtlCache<PrimaryNearMissAssignment>,
    var hits: Int,
    var misses: Int,
) : PrimaryNearMissAssignmentStore {
    override fun find(code: String): PrimaryNearMissAssignment? {
        val hit = (cache.get(code) as PrimaryNearMissAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNearMissAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNearMissAssignment> = inner.list()
}

fun newCachedPrimaryNearMissAssignmentStore(inner: PrimaryNearMissAssignmentStore, ttl: Int, clock: Clock): PrimaryNearMissAssignmentStore {
    return CachedPrimaryNearMissAssignmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryNearMissAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary near miss assignments. Empty fields match all. */
data class PrimaryNearMissAssignmentQuery(
    var source: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNearMissAssignment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryNearMissAssignment>): MutableList<PrimaryNearMissAssignment> {
        val matched = filter(items) { candidate: PrimaryNearMissAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNearMissAssignments(store: PrimaryNearMissAssignmentStore, query: PrimaryNearMissAssignmentQuery): MutableList<PrimaryNearMissAssignment> {
    return query.apply(store.list())
}
