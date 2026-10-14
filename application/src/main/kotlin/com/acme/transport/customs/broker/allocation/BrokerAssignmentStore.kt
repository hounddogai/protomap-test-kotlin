package com.acme.transport.customs.broker.allocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker assignments by code. */
interface BrokerAssignmentStore {
    fun find(code: String): BrokerAssignment?
    fun save(item: BrokerAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerAssignmentStore(
    var items: MutableMap<String, BrokerAssignment>,
    var order: MutableList<String>,
) : BrokerAssignmentStore {
    override fun find(code: String): BrokerAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerAssignment) {
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

    override fun list(): MutableList<BrokerAssignment> {
        val out = mutableListOf<BrokerAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerAssignmentStore(): BrokerAssignmentStore {
    return MemoryBrokerAssignmentStore(mutableMapOf<String, BrokerAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerAssignmentStore(
    var inner: BrokerAssignmentStore,
    var cache: TtlCache<BrokerAssignment>,
    var hits: Int,
    var misses: Int,
) : BrokerAssignmentStore {
    override fun find(code: String): BrokerAssignment? {
        val hit = (cache.get(code) as BrokerAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerAssignment> = inner.list()
}

fun newCachedBrokerAssignmentStore(inner: BrokerAssignmentStore, ttl: Int, clock: Clock): BrokerAssignmentStore {
    return CachedBrokerAssignmentStore(
        inner = inner,
        cache = newTtlCache<BrokerAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker assignments. Empty fields match all. */
data class BrokerAssignmentQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerAssignment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerAssignment>): MutableList<BrokerAssignment> {
        val matched = filter(items) { candidate: BrokerAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerAssignments(store: BrokerAssignmentStore, query: BrokerAssignmentQuery): MutableList<BrokerAssignment> {
    return query.apply(store.list())
}
