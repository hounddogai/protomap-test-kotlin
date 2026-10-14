package com.acme.transport.network.node.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default node assignments by code. */
interface DefaultNodeAssignmentStore {
    fun find(code: String): DefaultNodeAssignment?
    fun save(item: DefaultNodeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultNodeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultNodeAssignmentStore(
    var items: MutableMap<String, DefaultNodeAssignment>,
    var order: MutableList<String>,
) : DefaultNodeAssignmentStore {
    override fun find(code: String): DefaultNodeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultNodeAssignment) {
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

    override fun list(): MutableList<DefaultNodeAssignment> {
        val out = mutableListOf<DefaultNodeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultNodeAssignmentStore(): DefaultNodeAssignmentStore {
    return MemoryDefaultNodeAssignmentStore(
        mutableMapOf<String, DefaultNodeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultNodeAssignmentStore(
    var inner: DefaultNodeAssignmentStore,
    var cache: TtlCache<DefaultNodeAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultNodeAssignmentStore {
    override fun find(code: String): DefaultNodeAssignment? {
        val hit = (cache.get(code) as DefaultNodeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultNodeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultNodeAssignment> = inner.list()
}

fun newCachedDefaultNodeAssignmentStore(inner: DefaultNodeAssignmentStore, ttl: Int, clock: Clock): DefaultNodeAssignmentStore {
    return CachedDefaultNodeAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultNodeAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default node assignments. Empty fields match all. */
data class DefaultNodeAssignmentQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultNodeAssignment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultNodeAssignment>): MutableList<DefaultNodeAssignment> {
        val matched = filter(items) { candidate: DefaultNodeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultNodeAssignments(store: DefaultNodeAssignmentStore, query: DefaultNodeAssignmentQuery): MutableList<DefaultNodeAssignment> {
    return query.apply(store.list())
}
