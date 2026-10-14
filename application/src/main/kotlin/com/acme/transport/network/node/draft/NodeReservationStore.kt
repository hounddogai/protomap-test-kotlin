package com.acme.transport.network.node.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node reservations by code. */
interface NodeReservationStore {
    fun find(code: String): NodeReservation?
    fun save(item: NodeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeReservationStore(
    var items: MutableMap<String, NodeReservation>,
    var order: MutableList<String>,
) : NodeReservationStore {
    override fun find(code: String): NodeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeReservation) {
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

    override fun list(): MutableList<NodeReservation> {
        val out = mutableListOf<NodeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeReservationStore(): NodeReservationStore {
    return MemoryNodeReservationStore(mutableMapOf<String, NodeReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeReservationStore(
    var inner: NodeReservationStore,
    var cache: TtlCache<NodeReservation>,
    var hits: Int,
    var misses: Int,
) : NodeReservationStore {
    override fun find(code: String): NodeReservation? {
        val hit = (cache.get(code) as NodeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeReservation> = inner.list()
}

fun newCachedNodeReservationStore(inner: NodeReservationStore, ttl: Int, clock: Clock): NodeReservationStore {
    return CachedNodeReservationStore(
        inner = inner,
        cache = newTtlCache<NodeReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node reservations. Empty fields match all. */
data class NodeReservationQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<NodeReservation>): MutableList<NodeReservation> {
        val matched = filter(items) { candidate: NodeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeReservations(store: NodeReservationStore, query: NodeReservationQuery): MutableList<NodeReservation> {
    return query.apply(store.list())
}
