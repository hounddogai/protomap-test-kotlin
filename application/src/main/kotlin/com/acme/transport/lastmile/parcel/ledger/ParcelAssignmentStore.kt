package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel assignments by code. */
interface ParcelAssignmentStore {
    fun find(code: String): ParcelAssignment?
    fun save(item: ParcelAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelAssignmentStore(
    var items: MutableMap<String, ParcelAssignment>,
    var order: MutableList<String>,
) : ParcelAssignmentStore {
    override fun find(code: String): ParcelAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelAssignment) {
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

    override fun list(): MutableList<ParcelAssignment> {
        val out = mutableListOf<ParcelAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelAssignmentStore(): ParcelAssignmentStore {
    return MemoryParcelAssignmentStore(mutableMapOf<String, ParcelAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelAssignmentStore(
    var inner: ParcelAssignmentStore,
    var cache: TtlCache<ParcelAssignment>,
    var hits: Int,
    var misses: Int,
) : ParcelAssignmentStore {
    override fun find(code: String): ParcelAssignment? {
        val hit = (cache.get(code) as ParcelAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelAssignment> = inner.list()
}

fun newCachedParcelAssignmentStore(inner: ParcelAssignmentStore, ttl: Int, clock: Clock): ParcelAssignmentStore {
    return CachedParcelAssignmentStore(
        inner = inner,
        cache = newTtlCache<ParcelAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel assignments. Empty fields match all. */
data class ParcelAssignmentQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelAssignment>): MutableList<ParcelAssignment> {
        val matched = filter(items) { candidate: ParcelAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelAssignments(store: ParcelAssignmentStore, query: ParcelAssignmentQuery): MutableList<ParcelAssignment> {
    return query.apply(store.list())
}
