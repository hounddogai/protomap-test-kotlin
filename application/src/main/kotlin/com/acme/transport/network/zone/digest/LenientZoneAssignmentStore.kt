package com.acme.transport.network.zone.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient zone assignments by code. */
interface LenientZoneAssignmentStore {
    fun find(code: String): LenientZoneAssignment?
    fun save(item: LenientZoneAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientZoneAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientZoneAssignmentStore(
    var items: MutableMap<String, LenientZoneAssignment>,
    var order: MutableList<String>,
) : LenientZoneAssignmentStore {
    override fun find(code: String): LenientZoneAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientZoneAssignment) {
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

    override fun list(): MutableList<LenientZoneAssignment> {
        val out = mutableListOf<LenientZoneAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientZoneAssignmentStore(): LenientZoneAssignmentStore {
    return MemoryLenientZoneAssignmentStore(
        mutableMapOf<String, LenientZoneAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientZoneAssignmentStore(
    var inner: LenientZoneAssignmentStore,
    var cache: TtlCache<LenientZoneAssignment>,
    var hits: Int,
    var misses: Int,
) : LenientZoneAssignmentStore {
    override fun find(code: String): LenientZoneAssignment? {
        val hit = (cache.get(code) as LenientZoneAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientZoneAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientZoneAssignment> = inner.list()
}

fun newCachedLenientZoneAssignmentStore(inner: LenientZoneAssignmentStore, ttl: Int, clock: Clock): LenientZoneAssignmentStore {
    return CachedLenientZoneAssignmentStore(
        inner = inner,
        cache = newTtlCache<LenientZoneAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient zone assignments. Empty fields match all. */
data class LenientZoneAssignmentQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientZoneAssignment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientZoneAssignment>): MutableList<LenientZoneAssignment> {
        val matched = filter(items) { candidate: LenientZoneAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientZoneAssignments(store: LenientZoneAssignmentStore, query: LenientZoneAssignmentQuery): MutableList<LenientZoneAssignment> {
    return query.apply(store.list())
}
