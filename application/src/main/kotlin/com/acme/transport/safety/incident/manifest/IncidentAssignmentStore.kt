package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident assignments by code. */
interface IncidentAssignmentStore {
    fun find(code: String): IncidentAssignment?
    fun save(item: IncidentAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentAssignmentStore(
    var items: MutableMap<String, IncidentAssignment>,
    var order: MutableList<String>,
) : IncidentAssignmentStore {
    override fun find(code: String): IncidentAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentAssignment) {
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

    override fun list(): MutableList<IncidentAssignment> {
        val out = mutableListOf<IncidentAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentAssignmentStore(): IncidentAssignmentStore {
    return MemoryIncidentAssignmentStore(mutableMapOf<String, IncidentAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentAssignmentStore(
    var inner: IncidentAssignmentStore,
    var cache: TtlCache<IncidentAssignment>,
    var hits: Int,
    var misses: Int,
) : IncidentAssignmentStore {
    override fun find(code: String): IncidentAssignment? {
        val hit = (cache.get(code) as IncidentAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentAssignment> = inner.list()
}

fun newCachedIncidentAssignmentStore(inner: IncidentAssignmentStore, ttl: Int, clock: Clock): IncidentAssignmentStore {
    return CachedIncidentAssignmentStore(
        inner = inner,
        cache = newTtlCache<IncidentAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident assignments. Empty fields match all. */
data class IncidentAssignmentQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentAssignment>): MutableList<IncidentAssignment> {
        val matched = filter(items) { candidate: IncidentAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentAssignments(store: IncidentAssignmentStore, query: IncidentAssignmentQuery): MutableList<IncidentAssignment> {
    return query.apply(store.list())
}
