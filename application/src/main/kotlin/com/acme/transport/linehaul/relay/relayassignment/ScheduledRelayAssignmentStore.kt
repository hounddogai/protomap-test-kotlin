package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled relay assignments by code. */
interface ScheduledRelayAssignmentStore {
    fun find(code: String): ScheduledRelayAssignment?
    fun save(item: ScheduledRelayAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRelayAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRelayAssignmentStore(
    var items: MutableMap<String, ScheduledRelayAssignment>,
    var order: MutableList<String>,
) : ScheduledRelayAssignmentStore {
    override fun find(code: String): ScheduledRelayAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRelayAssignment) {
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

    override fun list(): MutableList<ScheduledRelayAssignment> {
        val out = mutableListOf<ScheduledRelayAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRelayAssignmentStore(): ScheduledRelayAssignmentStore {
    return MemoryScheduledRelayAssignmentStore(
        mutableMapOf<String, ScheduledRelayAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRelayAssignmentStore(
    var inner: ScheduledRelayAssignmentStore,
    var cache: TtlCache<ScheduledRelayAssignment>,
    var hits: Int,
    var misses: Int,
) : ScheduledRelayAssignmentStore {
    override fun find(code: String): ScheduledRelayAssignment? {
        val hit = (cache.get(code) as ScheduledRelayAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRelayAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRelayAssignment> = inner.list()
}

fun newCachedScheduledRelayAssignmentStore(inner: ScheduledRelayAssignmentStore, ttl: Int, clock: Clock): ScheduledRelayAssignmentStore {
    return CachedScheduledRelayAssignmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledRelayAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled relay assignments. Empty fields match all. */
data class ScheduledRelayAssignmentQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRelayAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRelayAssignment>): MutableList<ScheduledRelayAssignment> {
        val matched = filter(items) { candidate: ScheduledRelayAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRelayAssignments(store: ScheduledRelayAssignmentStore, query: ScheduledRelayAssignmentQuery): MutableList<ScheduledRelayAssignment> {
    return query.apply(store.list())
}
