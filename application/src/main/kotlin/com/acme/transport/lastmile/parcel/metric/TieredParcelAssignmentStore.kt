package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered parcel assignments by code. */
interface TieredParcelAssignmentStore {
    fun find(code: String): TieredParcelAssignment?
    fun save(item: TieredParcelAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredParcelAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredParcelAssignmentStore(
    var items: MutableMap<String, TieredParcelAssignment>,
    var order: MutableList<String>,
) : TieredParcelAssignmentStore {
    override fun find(code: String): TieredParcelAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredParcelAssignment) {
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

    override fun list(): MutableList<TieredParcelAssignment> {
        val out = mutableListOf<TieredParcelAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredParcelAssignmentStore(): TieredParcelAssignmentStore {
    return MemoryTieredParcelAssignmentStore(
        mutableMapOf<String, TieredParcelAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredParcelAssignmentStore(
    var inner: TieredParcelAssignmentStore,
    var cache: TtlCache<TieredParcelAssignment>,
    var hits: Int,
    var misses: Int,
) : TieredParcelAssignmentStore {
    override fun find(code: String): TieredParcelAssignment? {
        val hit = (cache.get(code) as TieredParcelAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredParcelAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredParcelAssignment> = inner.list()
}

fun newCachedTieredParcelAssignmentStore(inner: TieredParcelAssignmentStore, ttl: Int, clock: Clock): TieredParcelAssignmentStore {
    return CachedTieredParcelAssignmentStore(
        inner = inner,
        cache = newTtlCache<TieredParcelAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered parcel assignments. Empty fields match all. */
data class TieredParcelAssignmentQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredParcelAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredParcelAssignment>): MutableList<TieredParcelAssignment> {
        val matched = filter(items) { candidate: TieredParcelAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredParcelAssignments(store: TieredParcelAssignmentStore, query: TieredParcelAssignmentQuery): MutableList<TieredParcelAssignment> {
    return query.apply(store.list())
}
