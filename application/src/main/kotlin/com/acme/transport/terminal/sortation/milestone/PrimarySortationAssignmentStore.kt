package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary sortation assignments by code. */
interface PrimarySortationAssignmentStore {
    fun find(code: String): PrimarySortationAssignment?
    fun save(item: PrimarySortationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimarySortationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimarySortationAssignmentStore(
    var items: MutableMap<String, PrimarySortationAssignment>,
    var order: MutableList<String>,
) : PrimarySortationAssignmentStore {
    override fun find(code: String): PrimarySortationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimarySortationAssignment) {
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

    override fun list(): MutableList<PrimarySortationAssignment> {
        val out = mutableListOf<PrimarySortationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimarySortationAssignmentStore(): PrimarySortationAssignmentStore {
    return MemoryPrimarySortationAssignmentStore(
        mutableMapOf<String, PrimarySortationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimarySortationAssignmentStore(
    var inner: PrimarySortationAssignmentStore,
    var cache: TtlCache<PrimarySortationAssignment>,
    var hits: Int,
    var misses: Int,
) : PrimarySortationAssignmentStore {
    override fun find(code: String): PrimarySortationAssignment? {
        val hit = (cache.get(code) as PrimarySortationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimarySortationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimarySortationAssignment> = inner.list()
}

fun newCachedPrimarySortationAssignmentStore(inner: PrimarySortationAssignmentStore, ttl: Int, clock: Clock): PrimarySortationAssignmentStore {
    return CachedPrimarySortationAssignmentStore(
        inner = inner,
        cache = newTtlCache<PrimarySortationAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary sortation assignments. Empty fields match all. */
data class PrimarySortationAssignmentQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimarySortationAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimarySortationAssignment>): MutableList<PrimarySortationAssignment> {
        val matched = filter(items) { candidate: PrimarySortationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimarySortationAssignments(store: PrimarySortationAssignmentStore, query: PrimarySortationAssignmentQuery): MutableList<PrimarySortationAssignment> {
    return query.apply(store.list())
}
