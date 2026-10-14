package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity assignments by code. */
interface RegionalCapacityAssignmentStore {
    fun find(code: String): RegionalCapacityAssignment?
    fun save(item: RegionalCapacityAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityAssignmentStore(
    var items: MutableMap<String, RegionalCapacityAssignment>,
    var order: MutableList<String>,
) : RegionalCapacityAssignmentStore {
    override fun find(code: String): RegionalCapacityAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityAssignment) {
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

    override fun list(): MutableList<RegionalCapacityAssignment> {
        val out = mutableListOf<RegionalCapacityAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityAssignmentStore(): RegionalCapacityAssignmentStore {
    return MemoryRegionalCapacityAssignmentStore(
        mutableMapOf<String, RegionalCapacityAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityAssignmentStore(
    var inner: RegionalCapacityAssignmentStore,
    var cache: TtlCache<RegionalCapacityAssignment>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityAssignmentStore {
    override fun find(code: String): RegionalCapacityAssignment? {
        val hit = (cache.get(code) as RegionalCapacityAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityAssignment> = inner.list()
}

fun newCachedRegionalCapacityAssignmentStore(inner: RegionalCapacityAssignmentStore, ttl: Int, clock: Clock): RegionalCapacityAssignmentStore {
    return CachedRegionalCapacityAssignmentStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity assignments. Empty fields match all. */
data class RegionalCapacityAssignmentQuery(
    var source: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityAssignment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RegionalCapacityAssignment>): MutableList<RegionalCapacityAssignment> {
        val matched = filter(items) { candidate: RegionalCapacityAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityAssignments(store: RegionalCapacityAssignmentStore, query: RegionalCapacityAssignmentQuery): MutableList<RegionalCapacityAssignment> {
    return query.apply(store.list())
}
