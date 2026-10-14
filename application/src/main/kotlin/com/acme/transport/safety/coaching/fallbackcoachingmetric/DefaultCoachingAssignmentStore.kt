package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching assignments by code. */
interface DefaultCoachingAssignmentStore {
    fun find(code: String): DefaultCoachingAssignment?
    fun save(item: DefaultCoachingAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingAssignmentStore(
    var items: MutableMap<String, DefaultCoachingAssignment>,
    var order: MutableList<String>,
) : DefaultCoachingAssignmentStore {
    override fun find(code: String): DefaultCoachingAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingAssignment) {
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

    override fun list(): MutableList<DefaultCoachingAssignment> {
        val out = mutableListOf<DefaultCoachingAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingAssignmentStore(): DefaultCoachingAssignmentStore {
    return MemoryDefaultCoachingAssignmentStore(
        mutableMapOf<String, DefaultCoachingAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingAssignmentStore(
    var inner: DefaultCoachingAssignmentStore,
    var cache: TtlCache<DefaultCoachingAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingAssignmentStore {
    override fun find(code: String): DefaultCoachingAssignment? {
        val hit = (cache.get(code) as DefaultCoachingAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingAssignment> = inner.list()
}

fun newCachedDefaultCoachingAssignmentStore(inner: DefaultCoachingAssignmentStore, ttl: Int, clock: Clock): DefaultCoachingAssignmentStore {
    return CachedDefaultCoachingAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching assignments. Empty fields match all. */
data class DefaultCoachingAssignmentQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCoachingAssignment>): MutableList<DefaultCoachingAssignment> {
        val matched = filter(items) { candidate: DefaultCoachingAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingAssignments(store: DefaultCoachingAssignmentStore, query: DefaultCoachingAssignmentQuery): MutableList<DefaultCoachingAssignment> {
    return query.apply(store.list())
}
