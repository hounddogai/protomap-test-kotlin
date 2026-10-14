package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback tender assignments by code. */
interface FallbackTenderAssignmentStore {
    fun find(code: String): FallbackTenderAssignment?
    fun save(item: FallbackTenderAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTenderAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTenderAssignmentStore(
    var items: MutableMap<String, FallbackTenderAssignment>,
    var order: MutableList<String>,
) : FallbackTenderAssignmentStore {
    override fun find(code: String): FallbackTenderAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTenderAssignment) {
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

    override fun list(): MutableList<FallbackTenderAssignment> {
        val out = mutableListOf<FallbackTenderAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTenderAssignmentStore(): FallbackTenderAssignmentStore {
    return MemoryFallbackTenderAssignmentStore(
        mutableMapOf<String, FallbackTenderAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTenderAssignmentStore(
    var inner: FallbackTenderAssignmentStore,
    var cache: TtlCache<FallbackTenderAssignment>,
    var hits: Int,
    var misses: Int,
) : FallbackTenderAssignmentStore {
    override fun find(code: String): FallbackTenderAssignment? {
        val hit = (cache.get(code) as FallbackTenderAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTenderAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTenderAssignment> = inner.list()
}

fun newCachedFallbackTenderAssignmentStore(inner: FallbackTenderAssignmentStore, ttl: Int, clock: Clock): FallbackTenderAssignmentStore {
    return CachedFallbackTenderAssignmentStore(
        inner = inner,
        cache = newTtlCache<FallbackTenderAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tender assignments. Empty fields match all. */
data class FallbackTenderAssignmentQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTenderAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FallbackTenderAssignment>): MutableList<FallbackTenderAssignment> {
        val matched = filter(items) { candidate: FallbackTenderAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTenderAssignments(store: FallbackTenderAssignmentStore, query: FallbackTenderAssignmentQuery): MutableList<FallbackTenderAssignment> {
    return query.apply(store.list())
}
