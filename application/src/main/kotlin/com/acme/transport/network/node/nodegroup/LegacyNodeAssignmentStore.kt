package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy node assignments by code. */
interface LegacyNodeAssignmentStore {
    fun find(code: String): LegacyNodeAssignment?
    fun save(item: LegacyNodeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyNodeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyNodeAssignmentStore(
    var items: MutableMap<String, LegacyNodeAssignment>,
    var order: MutableList<String>,
) : LegacyNodeAssignmentStore {
    override fun find(code: String): LegacyNodeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyNodeAssignment) {
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

    override fun list(): MutableList<LegacyNodeAssignment> {
        val out = mutableListOf<LegacyNodeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyNodeAssignmentStore(): LegacyNodeAssignmentStore {
    return MemoryLegacyNodeAssignmentStore(
        mutableMapOf<String, LegacyNodeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyNodeAssignmentStore(
    var inner: LegacyNodeAssignmentStore,
    var cache: TtlCache<LegacyNodeAssignment>,
    var hits: Int,
    var misses: Int,
) : LegacyNodeAssignmentStore {
    override fun find(code: String): LegacyNodeAssignment? {
        val hit = (cache.get(code) as LegacyNodeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyNodeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyNodeAssignment> = inner.list()
}

fun newCachedLegacyNodeAssignmentStore(inner: LegacyNodeAssignmentStore, ttl: Int, clock: Clock): LegacyNodeAssignmentStore {
    return CachedLegacyNodeAssignmentStore(
        inner = inner,
        cache = newTtlCache<LegacyNodeAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy node assignments. Empty fields match all. */
data class LegacyNodeAssignmentQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyNodeAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyNodeAssignment>): MutableList<LegacyNodeAssignment> {
        val matched = filter(items) { candidate: LegacyNodeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyNodeAssignments(store: LegacyNodeAssignmentStore, query: LegacyNodeAssignmentQuery): MutableList<LegacyNodeAssignment> {
    return query.apply(store.list())
}
