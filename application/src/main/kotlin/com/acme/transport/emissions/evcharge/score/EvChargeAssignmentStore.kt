package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge assignments by code. */
interface EvChargeAssignmentStore {
    fun find(code: String): EvChargeAssignment?
    fun save(item: EvChargeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeAssignmentStore(
    var items: MutableMap<String, EvChargeAssignment>,
    var order: MutableList<String>,
) : EvChargeAssignmentStore {
    override fun find(code: String): EvChargeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeAssignment) {
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

    override fun list(): MutableList<EvChargeAssignment> {
        val out = mutableListOf<EvChargeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeAssignmentStore(): EvChargeAssignmentStore {
    return MemoryEvChargeAssignmentStore(mutableMapOf<String, EvChargeAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeAssignmentStore(
    var inner: EvChargeAssignmentStore,
    var cache: TtlCache<EvChargeAssignment>,
    var hits: Int,
    var misses: Int,
) : EvChargeAssignmentStore {
    override fun find(code: String): EvChargeAssignment? {
        val hit = (cache.get(code) as EvChargeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeAssignment> = inner.list()
}

fun newCachedEvChargeAssignmentStore(inner: EvChargeAssignmentStore, ttl: Int, clock: Clock): EvChargeAssignmentStore {
    return CachedEvChargeAssignmentStore(
        inner = inner,
        cache = newTtlCache<EvChargeAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge assignments. Empty fields match all. */
data class EvChargeAssignmentQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeAssignment>): MutableList<EvChargeAssignment> {
        val matched = filter(items) { candidate: EvChargeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeAssignments(store: EvChargeAssignmentStore, query: EvChargeAssignmentQuery): MutableList<EvChargeAssignment> {
    return query.apply(store.list())
}
