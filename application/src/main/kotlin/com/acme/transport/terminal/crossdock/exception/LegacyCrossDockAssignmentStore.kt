package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock assignments by code. */
interface LegacyCrossDockAssignmentStore {
    fun find(code: String): LegacyCrossDockAssignment?
    fun save(item: LegacyCrossDockAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockAssignmentStore(
    var items: MutableMap<String, LegacyCrossDockAssignment>,
    var order: MutableList<String>,
) : LegacyCrossDockAssignmentStore {
    override fun find(code: String): LegacyCrossDockAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockAssignment) {
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

    override fun list(): MutableList<LegacyCrossDockAssignment> {
        val out = mutableListOf<LegacyCrossDockAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockAssignmentStore(): LegacyCrossDockAssignmentStore {
    return MemoryLegacyCrossDockAssignmentStore(
        mutableMapOf<String, LegacyCrossDockAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockAssignmentStore(
    var inner: LegacyCrossDockAssignmentStore,
    var cache: TtlCache<LegacyCrossDockAssignment>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockAssignmentStore {
    override fun find(code: String): LegacyCrossDockAssignment? {
        val hit = (cache.get(code) as LegacyCrossDockAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockAssignment> = inner.list()
}

fun newCachedLegacyCrossDockAssignmentStore(inner: LegacyCrossDockAssignmentStore, ttl: Int, clock: Clock): LegacyCrossDockAssignmentStore {
    return CachedLegacyCrossDockAssignmentStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock assignments. Empty fields match all. */
data class LegacyCrossDockAssignmentQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCrossDockAssignment>): MutableList<LegacyCrossDockAssignment> {
        val matched = filter(items) { candidate: LegacyCrossDockAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockAssignments(store: LegacyCrossDockAssignmentStore, query: LegacyCrossDockAssignmentQuery): MutableList<LegacyCrossDockAssignment> {
    return query.apply(store.list())
}
