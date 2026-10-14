package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback defect assignments by code. */
interface FallbackDefectAssignmentStore {
    fun find(code: String): FallbackDefectAssignment?
    fun save(item: FallbackDefectAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDefectAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDefectAssignmentStore(
    var items: MutableMap<String, FallbackDefectAssignment>,
    var order: MutableList<String>,
) : FallbackDefectAssignmentStore {
    override fun find(code: String): FallbackDefectAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDefectAssignment) {
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

    override fun list(): MutableList<FallbackDefectAssignment> {
        val out = mutableListOf<FallbackDefectAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDefectAssignmentStore(): FallbackDefectAssignmentStore {
    return MemoryFallbackDefectAssignmentStore(
        mutableMapOf<String, FallbackDefectAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDefectAssignmentStore(
    var inner: FallbackDefectAssignmentStore,
    var cache: TtlCache<FallbackDefectAssignment>,
    var hits: Int,
    var misses: Int,
) : FallbackDefectAssignmentStore {
    override fun find(code: String): FallbackDefectAssignment? {
        val hit = (cache.get(code) as FallbackDefectAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDefectAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDefectAssignment> = inner.list()
}

fun newCachedFallbackDefectAssignmentStore(inner: FallbackDefectAssignmentStore, ttl: Int, clock: Clock): FallbackDefectAssignmentStore {
    return CachedFallbackDefectAssignmentStore(
        inner = inner,
        cache = newTtlCache<FallbackDefectAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback defect assignments. Empty fields match all. */
data class FallbackDefectAssignmentQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDefectAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDefectAssignment>): MutableList<FallbackDefectAssignment> {
        val matched = filter(items) { candidate: FallbackDefectAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDefectAssignments(store: FallbackDefectAssignmentStore, query: FallbackDefectAssignmentQuery): MutableList<FallbackDefectAssignment> {
    return query.apply(store.list())
}
