package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending parcel assignments by code. */
interface PendingParcelAssignmentStore {
    fun find(code: String): PendingParcelAssignment?
    fun save(item: PendingParcelAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingParcelAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingParcelAssignmentStore(
    var items: MutableMap<String, PendingParcelAssignment>,
    var order: MutableList<String>,
) : PendingParcelAssignmentStore {
    override fun find(code: String): PendingParcelAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingParcelAssignment) {
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

    override fun list(): MutableList<PendingParcelAssignment> {
        val out = mutableListOf<PendingParcelAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingParcelAssignmentStore(): PendingParcelAssignmentStore {
    return MemoryPendingParcelAssignmentStore(
        mutableMapOf<String, PendingParcelAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingParcelAssignmentStore(
    var inner: PendingParcelAssignmentStore,
    var cache: TtlCache<PendingParcelAssignment>,
    var hits: Int,
    var misses: Int,
) : PendingParcelAssignmentStore {
    override fun find(code: String): PendingParcelAssignment? {
        val hit = (cache.get(code) as PendingParcelAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingParcelAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingParcelAssignment> = inner.list()
}

fun newCachedPendingParcelAssignmentStore(inner: PendingParcelAssignmentStore, ttl: Int, clock: Clock): PendingParcelAssignmentStore {
    return CachedPendingParcelAssignmentStore(
        inner = inner,
        cache = newTtlCache<PendingParcelAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending parcel assignments. Empty fields match all. */
data class PendingParcelAssignmentQuery(
    var country: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingParcelAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingParcelAssignment>): MutableList<PendingParcelAssignment> {
        val matched = filter(items) { candidate: PendingParcelAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingParcelAssignments(store: PendingParcelAssignmentStore, query: PendingParcelAssignmentQuery): MutableList<PendingParcelAssignment> {
    return query.apply(store.list())
}
