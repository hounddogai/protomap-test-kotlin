package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global lease return assignments by code. */
interface GlobalLeaseReturnAssignmentStore {
    fun find(code: String): GlobalLeaseReturnAssignment?
    fun save(item: GlobalLeaseReturnAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLeaseReturnAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLeaseReturnAssignmentStore(
    var items: MutableMap<String, GlobalLeaseReturnAssignment>,
    var order: MutableList<String>,
) : GlobalLeaseReturnAssignmentStore {
    override fun find(code: String): GlobalLeaseReturnAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLeaseReturnAssignment) {
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

    override fun list(): MutableList<GlobalLeaseReturnAssignment> {
        val out = mutableListOf<GlobalLeaseReturnAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLeaseReturnAssignmentStore(): GlobalLeaseReturnAssignmentStore {
    return MemoryGlobalLeaseReturnAssignmentStore(
        mutableMapOf<String, GlobalLeaseReturnAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLeaseReturnAssignmentStore(
    var inner: GlobalLeaseReturnAssignmentStore,
    var cache: TtlCache<GlobalLeaseReturnAssignment>,
    var hits: Int,
    var misses: Int,
) : GlobalLeaseReturnAssignmentStore {
    override fun find(code: String): GlobalLeaseReturnAssignment? {
        val hit = (cache.get(code) as GlobalLeaseReturnAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLeaseReturnAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLeaseReturnAssignment> = inner.list()
}

fun newCachedGlobalLeaseReturnAssignmentStore(inner: GlobalLeaseReturnAssignmentStore, ttl: Int, clock: Clock): GlobalLeaseReturnAssignmentStore {
    return CachedGlobalLeaseReturnAssignmentStore(
        inner = inner,
        cache = newTtlCache<GlobalLeaseReturnAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global lease return assignments. Empty fields match all. */
data class GlobalLeaseReturnAssignmentQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLeaseReturnAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLeaseReturnAssignment>): MutableList<GlobalLeaseReturnAssignment> {
        val matched = filter(items) { candidate: GlobalLeaseReturnAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLeaseReturnAssignments(store: GlobalLeaseReturnAssignmentStore, query: GlobalLeaseReturnAssignmentQuery): MutableList<GlobalLeaseReturnAssignment> {
    return query.apply(store.list())
}
