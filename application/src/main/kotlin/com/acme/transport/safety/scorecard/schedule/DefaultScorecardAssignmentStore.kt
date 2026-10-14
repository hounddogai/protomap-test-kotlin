package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default scorecard assignments by code. */
interface DefaultScorecardAssignmentStore {
    fun find(code: String): DefaultScorecardAssignment?
    fun save(item: DefaultScorecardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultScorecardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultScorecardAssignmentStore(
    var items: MutableMap<String, DefaultScorecardAssignment>,
    var order: MutableList<String>,
) : DefaultScorecardAssignmentStore {
    override fun find(code: String): DefaultScorecardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultScorecardAssignment) {
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

    override fun list(): MutableList<DefaultScorecardAssignment> {
        val out = mutableListOf<DefaultScorecardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultScorecardAssignmentStore(): DefaultScorecardAssignmentStore {
    return MemoryDefaultScorecardAssignmentStore(
        mutableMapOf<String, DefaultScorecardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultScorecardAssignmentStore(
    var inner: DefaultScorecardAssignmentStore,
    var cache: TtlCache<DefaultScorecardAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultScorecardAssignmentStore {
    override fun find(code: String): DefaultScorecardAssignment? {
        val hit = (cache.get(code) as DefaultScorecardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultScorecardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultScorecardAssignment> = inner.list()
}

fun newCachedDefaultScorecardAssignmentStore(inner: DefaultScorecardAssignmentStore, ttl: Int, clock: Clock): DefaultScorecardAssignmentStore {
    return CachedDefaultScorecardAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultScorecardAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default scorecard assignments. Empty fields match all. */
data class DefaultScorecardAssignmentQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultScorecardAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<DefaultScorecardAssignment>): MutableList<DefaultScorecardAssignment> {
        val matched = filter(items) { candidate: DefaultScorecardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultScorecardAssignments(store: DefaultScorecardAssignmentStore, query: DefaultScorecardAssignmentQuery): MutableList<DefaultScorecardAssignment> {
    return query.apply(store.list())
}
