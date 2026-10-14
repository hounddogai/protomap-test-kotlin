package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling trailer assignments by code. */
interface RollingTrailerAssignmentStore {
    fun find(code: String): RollingTrailerAssignment?
    fun save(item: RollingTrailerAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerAssignmentStore(
    var items: MutableMap<String, RollingTrailerAssignment>,
    var order: MutableList<String>,
) : RollingTrailerAssignmentStore {
    override fun find(code: String): RollingTrailerAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerAssignment) {
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

    override fun list(): MutableList<RollingTrailerAssignment> {
        val out = mutableListOf<RollingTrailerAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerAssignmentStore(): RollingTrailerAssignmentStore {
    return MemoryRollingTrailerAssignmentStore(
        mutableMapOf<String, RollingTrailerAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerAssignmentStore(
    var inner: RollingTrailerAssignmentStore,
    var cache: TtlCache<RollingTrailerAssignment>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerAssignmentStore {
    override fun find(code: String): RollingTrailerAssignment? {
        val hit = (cache.get(code) as RollingTrailerAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerAssignment> = inner.list()
}

fun newCachedRollingTrailerAssignmentStore(inner: RollingTrailerAssignmentStore, ttl: Int, clock: Clock): RollingTrailerAssignmentStore {
    return CachedRollingTrailerAssignmentStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer assignments. Empty fields match all. */
data class RollingTrailerAssignmentQuery(
    var region: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTrailerAssignment>): MutableList<RollingTrailerAssignment> {
        val matched = filter(items) { candidate: RollingTrailerAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerAssignments(store: RollingTrailerAssignmentStore, query: RollingTrailerAssignmentQuery): MutableList<RollingTrailerAssignment> {
    return query.apply(store.list())
}
