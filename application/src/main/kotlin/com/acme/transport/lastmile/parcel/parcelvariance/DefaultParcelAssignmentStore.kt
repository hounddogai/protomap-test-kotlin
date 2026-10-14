package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel assignments by code. */
interface DefaultParcelAssignmentStore {
    fun find(code: String): DefaultParcelAssignment?
    fun save(item: DefaultParcelAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelAssignmentStore(
    var items: MutableMap<String, DefaultParcelAssignment>,
    var order: MutableList<String>,
) : DefaultParcelAssignmentStore {
    override fun find(code: String): DefaultParcelAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelAssignment) {
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

    override fun list(): MutableList<DefaultParcelAssignment> {
        val out = mutableListOf<DefaultParcelAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelAssignmentStore(): DefaultParcelAssignmentStore {
    return MemoryDefaultParcelAssignmentStore(
        mutableMapOf<String, DefaultParcelAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelAssignmentStore(
    var inner: DefaultParcelAssignmentStore,
    var cache: TtlCache<DefaultParcelAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelAssignmentStore {
    override fun find(code: String): DefaultParcelAssignment? {
        val hit = (cache.get(code) as DefaultParcelAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelAssignment> = inner.list()
}

fun newCachedDefaultParcelAssignmentStore(inner: DefaultParcelAssignmentStore, ttl: Int, clock: Clock): DefaultParcelAssignmentStore {
    return CachedDefaultParcelAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel assignments. Empty fields match all. */
data class DefaultParcelAssignmentQuery(
    var country: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DefaultParcelAssignment>): MutableList<DefaultParcelAssignment> {
        val matched = filter(items) { candidate: DefaultParcelAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelAssignments(store: DefaultParcelAssignmentStore, query: DefaultParcelAssignmentQuery): MutableList<DefaultParcelAssignment> {
    return query.apply(store.list())
}
