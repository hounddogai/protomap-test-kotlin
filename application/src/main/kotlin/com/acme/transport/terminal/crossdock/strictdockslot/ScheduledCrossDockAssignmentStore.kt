package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled cross dock assignments by code. */
interface ScheduledCrossDockAssignmentStore {
    fun find(code: String): ScheduledCrossDockAssignment?
    fun save(item: ScheduledCrossDockAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCrossDockAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCrossDockAssignmentStore(
    var items: MutableMap<String, ScheduledCrossDockAssignment>,
    var order: MutableList<String>,
) : ScheduledCrossDockAssignmentStore {
    override fun find(code: String): ScheduledCrossDockAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCrossDockAssignment) {
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

    override fun list(): MutableList<ScheduledCrossDockAssignment> {
        val out = mutableListOf<ScheduledCrossDockAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCrossDockAssignmentStore(): ScheduledCrossDockAssignmentStore {
    return MemoryScheduledCrossDockAssignmentStore(
        mutableMapOf<String, ScheduledCrossDockAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCrossDockAssignmentStore(
    var inner: ScheduledCrossDockAssignmentStore,
    var cache: TtlCache<ScheduledCrossDockAssignment>,
    var hits: Int,
    var misses: Int,
) : ScheduledCrossDockAssignmentStore {
    override fun find(code: String): ScheduledCrossDockAssignment? {
        val hit = (cache.get(code) as ScheduledCrossDockAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCrossDockAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCrossDockAssignment> = inner.list()
}

fun newCachedScheduledCrossDockAssignmentStore(inner: ScheduledCrossDockAssignmentStore, ttl: Int, clock: Clock): ScheduledCrossDockAssignmentStore {
    return CachedScheduledCrossDockAssignmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledCrossDockAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled cross dock assignments. Empty fields match all. */
data class ScheduledCrossDockAssignmentQuery(
    var region: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCrossDockAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCrossDockAssignment>): MutableList<ScheduledCrossDockAssignment> {
        val matched = filter(items) { candidate: ScheduledCrossDockAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCrossDockAssignments(store: ScheduledCrossDockAssignmentStore, query: ScheduledCrossDockAssignmentQuery): MutableList<ScheduledCrossDockAssignment> {
    return query.apply(store.list())
}
