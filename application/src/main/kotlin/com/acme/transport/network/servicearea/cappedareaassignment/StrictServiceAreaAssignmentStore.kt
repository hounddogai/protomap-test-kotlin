package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict service area assignments by code. */
interface StrictServiceAreaAssignmentStore {
    fun find(code: String): StrictServiceAreaAssignment?
    fun save(item: StrictServiceAreaAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictServiceAreaAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictServiceAreaAssignmentStore(
    var items: MutableMap<String, StrictServiceAreaAssignment>,
    var order: MutableList<String>,
) : StrictServiceAreaAssignmentStore {
    override fun find(code: String): StrictServiceAreaAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictServiceAreaAssignment) {
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

    override fun list(): MutableList<StrictServiceAreaAssignment> {
        val out = mutableListOf<StrictServiceAreaAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictServiceAreaAssignmentStore(): StrictServiceAreaAssignmentStore {
    return MemoryStrictServiceAreaAssignmentStore(
        mutableMapOf<String, StrictServiceAreaAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictServiceAreaAssignmentStore(
    var inner: StrictServiceAreaAssignmentStore,
    var cache: TtlCache<StrictServiceAreaAssignment>,
    var hits: Int,
    var misses: Int,
) : StrictServiceAreaAssignmentStore {
    override fun find(code: String): StrictServiceAreaAssignment? {
        val hit = (cache.get(code) as StrictServiceAreaAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictServiceAreaAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictServiceAreaAssignment> = inner.list()
}

fun newCachedStrictServiceAreaAssignmentStore(inner: StrictServiceAreaAssignmentStore, ttl: Int, clock: Clock): StrictServiceAreaAssignmentStore {
    return CachedStrictServiceAreaAssignmentStore(
        inner = inner,
        cache = newTtlCache<StrictServiceAreaAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict service area assignments. Empty fields match all. */
data class StrictServiceAreaAssignmentQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictServiceAreaAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictServiceAreaAssignment>): MutableList<StrictServiceAreaAssignment> {
        val matched = filter(items) { candidate: StrictServiceAreaAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictServiceAreaAssignments(store: StrictServiceAreaAssignmentStore, query: StrictServiceAreaAssignmentQuery): MutableList<StrictServiceAreaAssignment> {
    return query.apply(store.list())
}
