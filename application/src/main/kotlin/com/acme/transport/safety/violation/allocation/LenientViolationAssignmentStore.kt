package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient violation assignments by code. */
interface LenientViolationAssignmentStore {
    fun find(code: String): LenientViolationAssignment?
    fun save(item: LenientViolationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientViolationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientViolationAssignmentStore(
    var items: MutableMap<String, LenientViolationAssignment>,
    var order: MutableList<String>,
) : LenientViolationAssignmentStore {
    override fun find(code: String): LenientViolationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientViolationAssignment) {
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

    override fun list(): MutableList<LenientViolationAssignment> {
        val out = mutableListOf<LenientViolationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientViolationAssignmentStore(): LenientViolationAssignmentStore {
    return MemoryLenientViolationAssignmentStore(
        mutableMapOf<String, LenientViolationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientViolationAssignmentStore(
    var inner: LenientViolationAssignmentStore,
    var cache: TtlCache<LenientViolationAssignment>,
    var hits: Int,
    var misses: Int,
) : LenientViolationAssignmentStore {
    override fun find(code: String): LenientViolationAssignment? {
        val hit = (cache.get(code) as LenientViolationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientViolationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientViolationAssignment> = inner.list()
}

fun newCachedLenientViolationAssignmentStore(inner: LenientViolationAssignmentStore, ttl: Int, clock: Clock): LenientViolationAssignmentStore {
    return CachedLenientViolationAssignmentStore(
        inner = inner,
        cache = newTtlCache<LenientViolationAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient violation assignments. Empty fields match all. */
data class LenientViolationAssignmentQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientViolationAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientViolationAssignment>): MutableList<LenientViolationAssignment> {
        val matched = filter(items) { candidate: LenientViolationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientViolationAssignments(store: LenientViolationAssignmentStore, query: LenientViolationAssignmentQuery): MutableList<LenientViolationAssignment> {
    return query.apply(store.list())
}
