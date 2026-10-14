package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery assignments by code. */
interface RedeliveryAssignmentStore {
    fun find(code: String): RedeliveryAssignment?
    fun save(item: RedeliveryAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryAssignmentStore(
    var items: MutableMap<String, RedeliveryAssignment>,
    var order: MutableList<String>,
) : RedeliveryAssignmentStore {
    override fun find(code: String): RedeliveryAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryAssignment) {
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

    override fun list(): MutableList<RedeliveryAssignment> {
        val out = mutableListOf<RedeliveryAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryAssignmentStore(): RedeliveryAssignmentStore {
    return MemoryRedeliveryAssignmentStore(
        mutableMapOf<String, RedeliveryAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryAssignmentStore(
    var inner: RedeliveryAssignmentStore,
    var cache: TtlCache<RedeliveryAssignment>,
    var hits: Int,
    var misses: Int,
) : RedeliveryAssignmentStore {
    override fun find(code: String): RedeliveryAssignment? {
        val hit = (cache.get(code) as RedeliveryAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryAssignment> = inner.list()
}

fun newCachedRedeliveryAssignmentStore(inner: RedeliveryAssignmentStore, ttl: Int, clock: Clock): RedeliveryAssignmentStore {
    return CachedRedeliveryAssignmentStore(
        inner = inner,
        cache = newTtlCache<RedeliveryAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery assignments. Empty fields match all. */
data class RedeliveryAssignmentQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryAssignment>): MutableList<RedeliveryAssignment> {
        val matched = filter(items) { candidate: RedeliveryAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryAssignments(store: RedeliveryAssignmentStore, query: RedeliveryAssignmentQuery): MutableList<RedeliveryAssignment> {
    return query.apply(store.list())
}
