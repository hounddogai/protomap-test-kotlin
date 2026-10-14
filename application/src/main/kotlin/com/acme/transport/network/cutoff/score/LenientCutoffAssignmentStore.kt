package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient cutoff assignments by code. */
interface LenientCutoffAssignmentStore {
    fun find(code: String): LenientCutoffAssignment?
    fun save(item: LenientCutoffAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCutoffAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCutoffAssignmentStore(
    var items: MutableMap<String, LenientCutoffAssignment>,
    var order: MutableList<String>,
) : LenientCutoffAssignmentStore {
    override fun find(code: String): LenientCutoffAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCutoffAssignment) {
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

    override fun list(): MutableList<LenientCutoffAssignment> {
        val out = mutableListOf<LenientCutoffAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCutoffAssignmentStore(): LenientCutoffAssignmentStore {
    return MemoryLenientCutoffAssignmentStore(
        mutableMapOf<String, LenientCutoffAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCutoffAssignmentStore(
    var inner: LenientCutoffAssignmentStore,
    var cache: TtlCache<LenientCutoffAssignment>,
    var hits: Int,
    var misses: Int,
) : LenientCutoffAssignmentStore {
    override fun find(code: String): LenientCutoffAssignment? {
        val hit = (cache.get(code) as LenientCutoffAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCutoffAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCutoffAssignment> = inner.list()
}

fun newCachedLenientCutoffAssignmentStore(inner: LenientCutoffAssignmentStore, ttl: Int, clock: Clock): LenientCutoffAssignmentStore {
    return CachedLenientCutoffAssignmentStore(
        inner = inner,
        cache = newTtlCache<LenientCutoffAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cutoff assignments. Empty fields match all. */
data class LenientCutoffAssignmentQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCutoffAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCutoffAssignment>): MutableList<LenientCutoffAssignment> {
        val matched = filter(items) { candidate: LenientCutoffAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCutoffAssignments(store: LenientCutoffAssignmentStore, query: LenientCutoffAssignmentQuery): MutableList<LenientCutoffAssignment> {
    return query.apply(store.list())
}
