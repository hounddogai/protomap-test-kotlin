package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling assignments by code. */
interface IdlingAssignmentStore {
    fun find(code: String): IdlingAssignment?
    fun save(item: IdlingAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingAssignmentStore(
    var items: MutableMap<String, IdlingAssignment>,
    var order: MutableList<String>,
) : IdlingAssignmentStore {
    override fun find(code: String): IdlingAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingAssignment) {
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

    override fun list(): MutableList<IdlingAssignment> {
        val out = mutableListOf<IdlingAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingAssignmentStore(): IdlingAssignmentStore {
    return MemoryIdlingAssignmentStore(mutableMapOf<String, IdlingAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingAssignmentStore(
    var inner: IdlingAssignmentStore,
    var cache: TtlCache<IdlingAssignment>,
    var hits: Int,
    var misses: Int,
) : IdlingAssignmentStore {
    override fun find(code: String): IdlingAssignment? {
        val hit = (cache.get(code) as IdlingAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingAssignment> = inner.list()
}

fun newCachedIdlingAssignmentStore(inner: IdlingAssignmentStore, ttl: Int, clock: Clock): IdlingAssignmentStore {
    return CachedIdlingAssignmentStore(
        inner = inner,
        cache = newTtlCache<IdlingAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling assignments. Empty fields match all. */
data class IdlingAssignmentQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingAssignment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<IdlingAssignment>): MutableList<IdlingAssignment> {
        val matched = filter(items) { candidate: IdlingAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingAssignments(store: IdlingAssignmentStore, query: IdlingAssignmentQuery): MutableList<IdlingAssignment> {
    return query.apply(store.list())
}
