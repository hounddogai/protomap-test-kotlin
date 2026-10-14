package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered declaration assignments by code. */
interface TieredDeclarationAssignmentStore {
    fun find(code: String): TieredDeclarationAssignment?
    fun save(item: TieredDeclarationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDeclarationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDeclarationAssignmentStore(
    var items: MutableMap<String, TieredDeclarationAssignment>,
    var order: MutableList<String>,
) : TieredDeclarationAssignmentStore {
    override fun find(code: String): TieredDeclarationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDeclarationAssignment) {
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

    override fun list(): MutableList<TieredDeclarationAssignment> {
        val out = mutableListOf<TieredDeclarationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDeclarationAssignmentStore(): TieredDeclarationAssignmentStore {
    return MemoryTieredDeclarationAssignmentStore(
        mutableMapOf<String, TieredDeclarationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDeclarationAssignmentStore(
    var inner: TieredDeclarationAssignmentStore,
    var cache: TtlCache<TieredDeclarationAssignment>,
    var hits: Int,
    var misses: Int,
) : TieredDeclarationAssignmentStore {
    override fun find(code: String): TieredDeclarationAssignment? {
        val hit = (cache.get(code) as TieredDeclarationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDeclarationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDeclarationAssignment> = inner.list()
}

fun newCachedTieredDeclarationAssignmentStore(inner: TieredDeclarationAssignmentStore, ttl: Int, clock: Clock): TieredDeclarationAssignmentStore {
    return CachedTieredDeclarationAssignmentStore(
        inner = inner,
        cache = newTtlCache<TieredDeclarationAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered declaration assignments. Empty fields match all. */
data class TieredDeclarationAssignmentQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDeclarationAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDeclarationAssignment>): MutableList<TieredDeclarationAssignment> {
        val matched = filter(items) { candidate: TieredDeclarationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDeclarationAssignments(store: TieredDeclarationAssignmentStore, query: TieredDeclarationAssignmentQuery): MutableList<TieredDeclarationAssignment> {
    return query.apply(store.list())
}
