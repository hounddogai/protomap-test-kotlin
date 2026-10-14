package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped service area assignments by code. */
interface CappedServiceAreaAssignmentStore {
    fun find(code: String): CappedServiceAreaAssignment?
    fun save(item: CappedServiceAreaAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServiceAreaAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServiceAreaAssignmentStore(
    var items: MutableMap<String, CappedServiceAreaAssignment>,
    var order: MutableList<String>,
) : CappedServiceAreaAssignmentStore {
    override fun find(code: String): CappedServiceAreaAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServiceAreaAssignment) {
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

    override fun list(): MutableList<CappedServiceAreaAssignment> {
        val out = mutableListOf<CappedServiceAreaAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServiceAreaAssignmentStore(): CappedServiceAreaAssignmentStore {
    return MemoryCappedServiceAreaAssignmentStore(
        mutableMapOf<String, CappedServiceAreaAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServiceAreaAssignmentStore(
    var inner: CappedServiceAreaAssignmentStore,
    var cache: TtlCache<CappedServiceAreaAssignment>,
    var hits: Int,
    var misses: Int,
) : CappedServiceAreaAssignmentStore {
    override fun find(code: String): CappedServiceAreaAssignment? {
        val hit = (cache.get(code) as CappedServiceAreaAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServiceAreaAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServiceAreaAssignment> = inner.list()
}

fun newCachedCappedServiceAreaAssignmentStore(inner: CappedServiceAreaAssignmentStore, ttl: Int, clock: Clock): CappedServiceAreaAssignmentStore {
    return CachedCappedServiceAreaAssignmentStore(
        inner = inner,
        cache = newTtlCache<CappedServiceAreaAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service area assignments. Empty fields match all. */
data class CappedServiceAreaAssignmentQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServiceAreaAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedServiceAreaAssignment>): MutableList<CappedServiceAreaAssignment> {
        val matched = filter(items) { candidate: CappedServiceAreaAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServiceAreaAssignments(store: CappedServiceAreaAssignmentStore, query: CappedServiceAreaAssignmentQuery): MutableList<CappedServiceAreaAssignment> {
    return query.apply(store.list())
}
