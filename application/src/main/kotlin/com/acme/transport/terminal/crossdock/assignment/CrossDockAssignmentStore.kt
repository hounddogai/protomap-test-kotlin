package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock assignments by code. */
interface CrossDockAssignmentStore {
    fun find(code: String): CrossDockAssignment?
    fun save(item: CrossDockAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockAssignmentStore(
    var items: MutableMap<String, CrossDockAssignment>,
    var order: MutableList<String>,
) : CrossDockAssignmentStore {
    override fun find(code: String): CrossDockAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockAssignment) {
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

    override fun list(): MutableList<CrossDockAssignment> {
        val out = mutableListOf<CrossDockAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockAssignmentStore(): CrossDockAssignmentStore {
    return MemoryCrossDockAssignmentStore(mutableMapOf<String, CrossDockAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockAssignmentStore(
    var inner: CrossDockAssignmentStore,
    var cache: TtlCache<CrossDockAssignment>,
    var hits: Int,
    var misses: Int,
) : CrossDockAssignmentStore {
    override fun find(code: String): CrossDockAssignment? {
        val hit = (cache.get(code) as CrossDockAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockAssignment> = inner.list()
}

fun newCachedCrossDockAssignmentStore(inner: CrossDockAssignmentStore, ttl: Int, clock: Clock): CrossDockAssignmentStore {
    return CachedCrossDockAssignmentStore(
        inner = inner,
        cache = newTtlCache<CrossDockAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock assignments. Empty fields match all. */
data class CrossDockAssignmentQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockAssignment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<CrossDockAssignment>): MutableList<CrossDockAssignment> {
        val matched = filter(items) { candidate: CrossDockAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockAssignments(store: CrossDockAssignmentStore, query: CrossDockAssignmentQuery): MutableList<CrossDockAssignment> {
    return query.apply(store.list())
}
