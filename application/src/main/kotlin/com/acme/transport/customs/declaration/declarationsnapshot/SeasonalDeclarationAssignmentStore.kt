package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal declaration assignments by code. */
interface SeasonalDeclarationAssignmentStore {
    fun find(code: String): SeasonalDeclarationAssignment?
    fun save(item: SeasonalDeclarationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDeclarationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDeclarationAssignmentStore(
    var items: MutableMap<String, SeasonalDeclarationAssignment>,
    var order: MutableList<String>,
) : SeasonalDeclarationAssignmentStore {
    override fun find(code: String): SeasonalDeclarationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDeclarationAssignment) {
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

    override fun list(): MutableList<SeasonalDeclarationAssignment> {
        val out = mutableListOf<SeasonalDeclarationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDeclarationAssignmentStore(): SeasonalDeclarationAssignmentStore {
    return MemorySeasonalDeclarationAssignmentStore(
        mutableMapOf<String, SeasonalDeclarationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDeclarationAssignmentStore(
    var inner: SeasonalDeclarationAssignmentStore,
    var cache: TtlCache<SeasonalDeclarationAssignment>,
    var hits: Int,
    var misses: Int,
) : SeasonalDeclarationAssignmentStore {
    override fun find(code: String): SeasonalDeclarationAssignment? {
        val hit = (cache.get(code) as SeasonalDeclarationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDeclarationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDeclarationAssignment> = inner.list()
}

fun newCachedSeasonalDeclarationAssignmentStore(inner: SeasonalDeclarationAssignmentStore, ttl: Int, clock: Clock): SeasonalDeclarationAssignmentStore {
    return CachedSeasonalDeclarationAssignmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalDeclarationAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal declaration assignments. Empty fields match all. */
data class SeasonalDeclarationAssignmentQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDeclarationAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDeclarationAssignment>): MutableList<SeasonalDeclarationAssignment> {
        val matched = filter(items) { candidate: SeasonalDeclarationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDeclarationAssignments(store: SeasonalDeclarationAssignmentStore, query: SeasonalDeclarationAssignmentQuery): MutableList<SeasonalDeclarationAssignment> {
    return query.apply(store.list())
}
