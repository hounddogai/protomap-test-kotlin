package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual idling assignments by code. */
interface ManualIdlingAssignmentStore {
    fun find(code: String): ManualIdlingAssignment?
    fun save(item: ManualIdlingAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualIdlingAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualIdlingAssignmentStore(
    var items: MutableMap<String, ManualIdlingAssignment>,
    var order: MutableList<String>,
) : ManualIdlingAssignmentStore {
    override fun find(code: String): ManualIdlingAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualIdlingAssignment) {
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

    override fun list(): MutableList<ManualIdlingAssignment> {
        val out = mutableListOf<ManualIdlingAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualIdlingAssignmentStore(): ManualIdlingAssignmentStore {
    return MemoryManualIdlingAssignmentStore(
        mutableMapOf<String, ManualIdlingAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualIdlingAssignmentStore(
    var inner: ManualIdlingAssignmentStore,
    var cache: TtlCache<ManualIdlingAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualIdlingAssignmentStore {
    override fun find(code: String): ManualIdlingAssignment? {
        val hit = (cache.get(code) as ManualIdlingAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualIdlingAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualIdlingAssignment> = inner.list()
}

fun newCachedManualIdlingAssignmentStore(inner: ManualIdlingAssignmentStore, ttl: Int, clock: Clock): ManualIdlingAssignmentStore {
    return CachedManualIdlingAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualIdlingAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual idling assignments. Empty fields match all. */
data class ManualIdlingAssignmentQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualIdlingAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualIdlingAssignment>): MutableList<ManualIdlingAssignment> {
        val matched = filter(items) { candidate: ManualIdlingAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualIdlingAssignments(store: ManualIdlingAssignmentStore, query: ManualIdlingAssignmentQuery): MutableList<ManualIdlingAssignment> {
    return query.apply(store.list())
}
