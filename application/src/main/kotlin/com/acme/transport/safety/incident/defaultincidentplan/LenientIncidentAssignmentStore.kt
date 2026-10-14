package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient incident assignments by code. */
interface LenientIncidentAssignmentStore {
    fun find(code: String): LenientIncidentAssignment?
    fun save(item: LenientIncidentAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIncidentAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIncidentAssignmentStore(
    var items: MutableMap<String, LenientIncidentAssignment>,
    var order: MutableList<String>,
) : LenientIncidentAssignmentStore {
    override fun find(code: String): LenientIncidentAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIncidentAssignment) {
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

    override fun list(): MutableList<LenientIncidentAssignment> {
        val out = mutableListOf<LenientIncidentAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIncidentAssignmentStore(): LenientIncidentAssignmentStore {
    return MemoryLenientIncidentAssignmentStore(
        mutableMapOf<String, LenientIncidentAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIncidentAssignmentStore(
    var inner: LenientIncidentAssignmentStore,
    var cache: TtlCache<LenientIncidentAssignment>,
    var hits: Int,
    var misses: Int,
) : LenientIncidentAssignmentStore {
    override fun find(code: String): LenientIncidentAssignment? {
        val hit = (cache.get(code) as LenientIncidentAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIncidentAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIncidentAssignment> = inner.list()
}

fun newCachedLenientIncidentAssignmentStore(inner: LenientIncidentAssignmentStore, ttl: Int, clock: Clock): LenientIncidentAssignmentStore {
    return CachedLenientIncidentAssignmentStore(
        inner = inner,
        cache = newTtlCache<LenientIncidentAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient incident assignments. Empty fields match all. */
data class LenientIncidentAssignmentQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIncidentAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientIncidentAssignment>): MutableList<LenientIncidentAssignment> {
        val matched = filter(items) { candidate: LenientIncidentAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIncidentAssignments(store: LenientIncidentAssignmentStore, query: LenientIncidentAssignmentQuery): MutableList<LenientIncidentAssignment> {
    return query.apply(store.list())
}
