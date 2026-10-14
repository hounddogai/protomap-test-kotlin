package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global parcel assignments by code. */
interface GlobalParcelAssignmentStore {
    fun find(code: String): GlobalParcelAssignment?
    fun save(item: GlobalParcelAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalParcelAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalParcelAssignmentStore(
    var items: MutableMap<String, GlobalParcelAssignment>,
    var order: MutableList<String>,
) : GlobalParcelAssignmentStore {
    override fun find(code: String): GlobalParcelAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalParcelAssignment) {
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

    override fun list(): MutableList<GlobalParcelAssignment> {
        val out = mutableListOf<GlobalParcelAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalParcelAssignmentStore(): GlobalParcelAssignmentStore {
    return MemoryGlobalParcelAssignmentStore(
        mutableMapOf<String, GlobalParcelAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalParcelAssignmentStore(
    var inner: GlobalParcelAssignmentStore,
    var cache: TtlCache<GlobalParcelAssignment>,
    var hits: Int,
    var misses: Int,
) : GlobalParcelAssignmentStore {
    override fun find(code: String): GlobalParcelAssignment? {
        val hit = (cache.get(code) as GlobalParcelAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalParcelAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalParcelAssignment> = inner.list()
}

fun newCachedGlobalParcelAssignmentStore(inner: GlobalParcelAssignmentStore, ttl: Int, clock: Clock): GlobalParcelAssignmentStore {
    return CachedGlobalParcelAssignmentStore(
        inner = inner,
        cache = newTtlCache<GlobalParcelAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global parcel assignments. Empty fields match all. */
data class GlobalParcelAssignmentQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalParcelAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalParcelAssignment>): MutableList<GlobalParcelAssignment> {
        val matched = filter(items) { candidate: GlobalParcelAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalParcelAssignments(store: GlobalParcelAssignmentStore, query: GlobalParcelAssignmentQuery): MutableList<GlobalParcelAssignment> {
    return query.apply(store.list())
}
