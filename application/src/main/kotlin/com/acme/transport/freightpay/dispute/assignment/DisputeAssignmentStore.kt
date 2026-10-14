package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute assignments by code. */
interface DisputeAssignmentStore {
    fun find(code: String): DisputeAssignment?
    fun save(item: DisputeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeAssignmentStore(
    var items: MutableMap<String, DisputeAssignment>,
    var order: MutableList<String>,
) : DisputeAssignmentStore {
    override fun find(code: String): DisputeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeAssignment) {
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

    override fun list(): MutableList<DisputeAssignment> {
        val out = mutableListOf<DisputeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeAssignmentStore(): DisputeAssignmentStore {
    return MemoryDisputeAssignmentStore(mutableMapOf<String, DisputeAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeAssignmentStore(
    var inner: DisputeAssignmentStore,
    var cache: TtlCache<DisputeAssignment>,
    var hits: Int,
    var misses: Int,
) : DisputeAssignmentStore {
    override fun find(code: String): DisputeAssignment? {
        val hit = (cache.get(code) as DisputeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeAssignment> = inner.list()
}

fun newCachedDisputeAssignmentStore(inner: DisputeAssignmentStore, ttl: Int, clock: Clock): DisputeAssignmentStore {
    return CachedDisputeAssignmentStore(
        inner = inner,
        cache = newTtlCache<DisputeAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute assignments. Empty fields match all. */
data class DisputeAssignmentQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeAssignment>): MutableList<DisputeAssignment> {
        val matched = filter(items) { candidate: DisputeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeAssignments(store: DisputeAssignmentStore, query: DisputeAssignmentQuery): MutableList<DisputeAssignment> {
    return query.apply(store.list())
}
