package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending qualification assignments by code. */
interface PendingQualificationAssignmentStore {
    fun find(code: String): PendingQualificationAssignment?
    fun save(item: PendingQualificationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingQualificationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingQualificationAssignmentStore(
    var items: MutableMap<String, PendingQualificationAssignment>,
    var order: MutableList<String>,
) : PendingQualificationAssignmentStore {
    override fun find(code: String): PendingQualificationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingQualificationAssignment) {
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

    override fun list(): MutableList<PendingQualificationAssignment> {
        val out = mutableListOf<PendingQualificationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingQualificationAssignmentStore(): PendingQualificationAssignmentStore {
    return MemoryPendingQualificationAssignmentStore(
        mutableMapOf<String, PendingQualificationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingQualificationAssignmentStore(
    var inner: PendingQualificationAssignmentStore,
    var cache: TtlCache<PendingQualificationAssignment>,
    var hits: Int,
    var misses: Int,
) : PendingQualificationAssignmentStore {
    override fun find(code: String): PendingQualificationAssignment? {
        val hit = (cache.get(code) as PendingQualificationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingQualificationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingQualificationAssignment> = inner.list()
}

fun newCachedPendingQualificationAssignmentStore(inner: PendingQualificationAssignmentStore, ttl: Int, clock: Clock): PendingQualificationAssignmentStore {
    return CachedPendingQualificationAssignmentStore(
        inner = inner,
        cache = newTtlCache<PendingQualificationAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending qualification assignments. Empty fields match all. */
data class PendingQualificationAssignmentQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingQualificationAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingQualificationAssignment>): MutableList<PendingQualificationAssignment> {
        val matched = filter(items) { candidate: PendingQualificationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingQualificationAssignments(store: PendingQualificationAssignmentStore, query: PendingQualificationAssignmentQuery): MutableList<PendingQualificationAssignment> {
    return query.apply(store.list())
}
