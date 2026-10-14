package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt assignments by code. */
interface AttemptAssignmentStore {
    fun find(code: String): AttemptAssignment?
    fun save(item: AttemptAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptAssignmentStore(
    var items: MutableMap<String, AttemptAssignment>,
    var order: MutableList<String>,
) : AttemptAssignmentStore {
    override fun find(code: String): AttemptAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptAssignment) {
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

    override fun list(): MutableList<AttemptAssignment> {
        val out = mutableListOf<AttemptAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptAssignmentStore(): AttemptAssignmentStore {
    return MemoryAttemptAssignmentStore(mutableMapOf<String, AttemptAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptAssignmentStore(
    var inner: AttemptAssignmentStore,
    var cache: TtlCache<AttemptAssignment>,
    var hits: Int,
    var misses: Int,
) : AttemptAssignmentStore {
    override fun find(code: String): AttemptAssignment? {
        val hit = (cache.get(code) as AttemptAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptAssignment> = inner.list()
}

fun newCachedAttemptAssignmentStore(inner: AttemptAssignmentStore, ttl: Int, clock: Clock): AttemptAssignmentStore {
    return CachedAttemptAssignmentStore(
        inner = inner,
        cache = newTtlCache<AttemptAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt assignments. Empty fields match all. */
data class AttemptAssignmentQuery(
    var region: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptAssignment>): MutableList<AttemptAssignment> {
        val matched = filter(items) { candidate: AttemptAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptAssignments(store: AttemptAssignmentStore, query: AttemptAssignmentQuery): MutableList<AttemptAssignment> {
    return query.apply(store.list())
}
