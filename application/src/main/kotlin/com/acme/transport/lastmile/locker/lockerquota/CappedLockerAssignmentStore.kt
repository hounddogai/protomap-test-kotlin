package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped locker assignments by code. */
interface CappedLockerAssignmentStore {
    fun find(code: String): CappedLockerAssignment?
    fun save(item: CappedLockerAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLockerAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLockerAssignmentStore(
    var items: MutableMap<String, CappedLockerAssignment>,
    var order: MutableList<String>,
) : CappedLockerAssignmentStore {
    override fun find(code: String): CappedLockerAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLockerAssignment) {
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

    override fun list(): MutableList<CappedLockerAssignment> {
        val out = mutableListOf<CappedLockerAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLockerAssignmentStore(): CappedLockerAssignmentStore {
    return MemoryCappedLockerAssignmentStore(
        mutableMapOf<String, CappedLockerAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLockerAssignmentStore(
    var inner: CappedLockerAssignmentStore,
    var cache: TtlCache<CappedLockerAssignment>,
    var hits: Int,
    var misses: Int,
) : CappedLockerAssignmentStore {
    override fun find(code: String): CappedLockerAssignment? {
        val hit = (cache.get(code) as CappedLockerAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLockerAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLockerAssignment> = inner.list()
}

fun newCachedCappedLockerAssignmentStore(inner: CappedLockerAssignmentStore, ttl: Int, clock: Clock): CappedLockerAssignmentStore {
    return CachedCappedLockerAssignmentStore(
        inner = inner,
        cache = newTtlCache<CappedLockerAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped locker assignments. Empty fields match all. */
data class CappedLockerAssignmentQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLockerAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CappedLockerAssignment>): MutableList<CappedLockerAssignment> {
        val matched = filter(items) { candidate: CappedLockerAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLockerAssignments(store: CappedLockerAssignmentStore, query: CappedLockerAssignmentQuery): MutableList<CappedLockerAssignment> {
    return query.apply(store.list())
}
