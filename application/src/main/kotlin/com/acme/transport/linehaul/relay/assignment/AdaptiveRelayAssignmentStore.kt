package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive relay assignments by code. */
interface AdaptiveRelayAssignmentStore {
    fun find(code: String): AdaptiveRelayAssignment?
    fun save(item: AdaptiveRelayAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRelayAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRelayAssignmentStore(
    var items: MutableMap<String, AdaptiveRelayAssignment>,
    var order: MutableList<String>,
) : AdaptiveRelayAssignmentStore {
    override fun find(code: String): AdaptiveRelayAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRelayAssignment) {
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

    override fun list(): MutableList<AdaptiveRelayAssignment> {
        val out = mutableListOf<AdaptiveRelayAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRelayAssignmentStore(): AdaptiveRelayAssignmentStore {
    return MemoryAdaptiveRelayAssignmentStore(
        mutableMapOf<String, AdaptiveRelayAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRelayAssignmentStore(
    var inner: AdaptiveRelayAssignmentStore,
    var cache: TtlCache<AdaptiveRelayAssignment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRelayAssignmentStore {
    override fun find(code: String): AdaptiveRelayAssignment? {
        val hit = (cache.get(code) as AdaptiveRelayAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRelayAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRelayAssignment> = inner.list()
}

fun newCachedAdaptiveRelayAssignmentStore(inner: AdaptiveRelayAssignmentStore, ttl: Int, clock: Clock): AdaptiveRelayAssignmentStore {
    return CachedAdaptiveRelayAssignmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRelayAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive relay assignments. Empty fields match all. */
data class AdaptiveRelayAssignmentQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRelayAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<AdaptiveRelayAssignment>): MutableList<AdaptiveRelayAssignment> {
        val matched = filter(items) { candidate: AdaptiveRelayAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRelayAssignments(store: AdaptiveRelayAssignmentStore, query: AdaptiveRelayAssignmentQuery): MutableList<AdaptiveRelayAssignment> {
    return query.apply(store.list())
}
