package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual trailer yard assignments by code. */
interface ManualTrailerYardAssignmentStore {
    fun find(code: String): ManualTrailerYardAssignment?
    fun save(item: ManualTrailerYardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerYardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerYardAssignmentStore(
    var items: MutableMap<String, ManualTrailerYardAssignment>,
    var order: MutableList<String>,
) : ManualTrailerYardAssignmentStore {
    override fun find(code: String): ManualTrailerYardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerYardAssignment) {
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

    override fun list(): MutableList<ManualTrailerYardAssignment> {
        val out = mutableListOf<ManualTrailerYardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerYardAssignmentStore(): ManualTrailerYardAssignmentStore {
    return MemoryManualTrailerYardAssignmentStore(
        mutableMapOf<String, ManualTrailerYardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerYardAssignmentStore(
    var inner: ManualTrailerYardAssignmentStore,
    var cache: TtlCache<ManualTrailerYardAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerYardAssignmentStore {
    override fun find(code: String): ManualTrailerYardAssignment? {
        val hit = (cache.get(code) as ManualTrailerYardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerYardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerYardAssignment> = inner.list()
}

fun newCachedManualTrailerYardAssignmentStore(inner: ManualTrailerYardAssignmentStore, ttl: Int, clock: Clock): ManualTrailerYardAssignmentStore {
    return CachedManualTrailerYardAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerYardAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer yard assignments. Empty fields match all. */
data class ManualTrailerYardAssignmentQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerYardAssignment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTrailerYardAssignment>): MutableList<ManualTrailerYardAssignment> {
        val matched = filter(items) { candidate: ManualTrailerYardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerYardAssignments(store: ManualTrailerYardAssignmentStore, query: ManualTrailerYardAssignmentQuery): MutableList<ManualTrailerYardAssignment> {
    return query.apply(store.list())
}
