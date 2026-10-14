package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender assignments by code. */
interface TenderAssignmentStore {
    fun find(code: String): TenderAssignment?
    fun save(item: TenderAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderAssignmentStore(
    var items: MutableMap<String, TenderAssignment>,
    var order: MutableList<String>,
) : TenderAssignmentStore {
    override fun find(code: String): TenderAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderAssignment) {
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

    override fun list(): MutableList<TenderAssignment> {
        val out = mutableListOf<TenderAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderAssignmentStore(): TenderAssignmentStore {
    return MemoryTenderAssignmentStore(mutableMapOf<String, TenderAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderAssignmentStore(
    var inner: TenderAssignmentStore,
    var cache: TtlCache<TenderAssignment>,
    var hits: Int,
    var misses: Int,
) : TenderAssignmentStore {
    override fun find(code: String): TenderAssignment? {
        val hit = (cache.get(code) as TenderAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderAssignment> = inner.list()
}

fun newCachedTenderAssignmentStore(inner: TenderAssignmentStore, ttl: Int, clock: Clock): TenderAssignmentStore {
    return CachedTenderAssignmentStore(
        inner = inner,
        cache = newTtlCache<TenderAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender assignments. Empty fields match all. */
data class TenderAssignmentQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderAssignment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<TenderAssignment>): MutableList<TenderAssignment> {
        val matched = filter(items) { candidate: TenderAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderAssignments(store: TenderAssignmentStore, query: TenderAssignmentQuery): MutableList<TenderAssignment> {
    return query.apply(store.list())
}
