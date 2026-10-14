package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker assignments by code. */
interface LockerAssignmentStore {
    fun find(code: String): LockerAssignment?
    fun save(item: LockerAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerAssignmentStore(
    var items: MutableMap<String, LockerAssignment>,
    var order: MutableList<String>,
) : LockerAssignmentStore {
    override fun find(code: String): LockerAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerAssignment) {
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

    override fun list(): MutableList<LockerAssignment> {
        val out = mutableListOf<LockerAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerAssignmentStore(): LockerAssignmentStore {
    return MemoryLockerAssignmentStore(mutableMapOf<String, LockerAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerAssignmentStore(
    var inner: LockerAssignmentStore,
    var cache: TtlCache<LockerAssignment>,
    var hits: Int,
    var misses: Int,
) : LockerAssignmentStore {
    override fun find(code: String): LockerAssignment? {
        val hit = (cache.get(code) as LockerAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerAssignment> = inner.list()
}

fun newCachedLockerAssignmentStore(inner: LockerAssignmentStore, ttl: Int, clock: Clock): LockerAssignmentStore {
    return CachedLockerAssignmentStore(
        inner = inner,
        cache = newTtlCache<LockerAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker assignments. Empty fields match all. */
data class LockerAssignmentQuery(
    var category: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerAssignment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerAssignment>): MutableList<LockerAssignment> {
        val matched = filter(items) { candidate: LockerAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerAssignments(store: LockerAssignmentStore, query: LockerAssignmentQuery): MutableList<LockerAssignment> {
    return query.apply(store.list())
}
