package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area assignments by code. */
interface ServiceAreaAssignmentStore {
    fun find(code: String): ServiceAreaAssignment?
    fun save(item: ServiceAreaAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaAssignmentStore(
    var items: MutableMap<String, ServiceAreaAssignment>,
    var order: MutableList<String>,
) : ServiceAreaAssignmentStore {
    override fun find(code: String): ServiceAreaAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaAssignment) {
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

    override fun list(): MutableList<ServiceAreaAssignment> {
        val out = mutableListOf<ServiceAreaAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaAssignmentStore(): ServiceAreaAssignmentStore {
    return MemoryServiceAreaAssignmentStore(
        mutableMapOf<String, ServiceAreaAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaAssignmentStore(
    var inner: ServiceAreaAssignmentStore,
    var cache: TtlCache<ServiceAreaAssignment>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaAssignmentStore {
    override fun find(code: String): ServiceAreaAssignment? {
        val hit = (cache.get(code) as ServiceAreaAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaAssignment> = inner.list()
}

fun newCachedServiceAreaAssignmentStore(inner: ServiceAreaAssignmentStore, ttl: Int, clock: Clock): ServiceAreaAssignmentStore {
    return CachedServiceAreaAssignmentStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area assignments. Empty fields match all. */
data class ServiceAreaAssignmentQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaAssignment>): MutableList<ServiceAreaAssignment> {
        val matched = filter(items) { candidate: ServiceAreaAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaAssignments(store: ServiceAreaAssignmentStore, query: ServiceAreaAssignmentQuery): MutableList<ServiceAreaAssignment> {
    return query.apply(store.list())
}
