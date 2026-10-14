package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive locker assignments by code. */
interface AdaptiveLockerAssignmentStore {
    fun find(code: String): AdaptiveLockerAssignment?
    fun save(item: AdaptiveLockerAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLockerAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLockerAssignmentStore(
    var items: MutableMap<String, AdaptiveLockerAssignment>,
    var order: MutableList<String>,
) : AdaptiveLockerAssignmentStore {
    override fun find(code: String): AdaptiveLockerAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLockerAssignment) {
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

    override fun list(): MutableList<AdaptiveLockerAssignment> {
        val out = mutableListOf<AdaptiveLockerAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLockerAssignmentStore(): AdaptiveLockerAssignmentStore {
    return MemoryAdaptiveLockerAssignmentStore(
        mutableMapOf<String, AdaptiveLockerAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLockerAssignmentStore(
    var inner: AdaptiveLockerAssignmentStore,
    var cache: TtlCache<AdaptiveLockerAssignment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLockerAssignmentStore {
    override fun find(code: String): AdaptiveLockerAssignment? {
        val hit = (cache.get(code) as AdaptiveLockerAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLockerAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLockerAssignment> = inner.list()
}

fun newCachedAdaptiveLockerAssignmentStore(inner: AdaptiveLockerAssignmentStore, ttl: Int, clock: Clock): AdaptiveLockerAssignmentStore {
    return CachedAdaptiveLockerAssignmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLockerAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive locker assignments. Empty fields match all. */
data class AdaptiveLockerAssignmentQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLockerAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<AdaptiveLockerAssignment>): MutableList<AdaptiveLockerAssignment> {
        val matched = filter(items) { candidate: AdaptiveLockerAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLockerAssignments(store: AdaptiveLockerAssignmentStore, query: AdaptiveLockerAssignmentQuery): MutableList<AdaptiveLockerAssignment> {
    return query.apply(store.list())
}
