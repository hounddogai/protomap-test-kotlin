package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification assignments by code. */
interface QualificationAssignmentStore {
    fun find(code: String): QualificationAssignment?
    fun save(item: QualificationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationAssignmentStore(
    var items: MutableMap<String, QualificationAssignment>,
    var order: MutableList<String>,
) : QualificationAssignmentStore {
    override fun find(code: String): QualificationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationAssignment) {
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

    override fun list(): MutableList<QualificationAssignment> {
        val out = mutableListOf<QualificationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationAssignmentStore(): QualificationAssignmentStore {
    return MemoryQualificationAssignmentStore(
        mutableMapOf<String, QualificationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationAssignmentStore(
    var inner: QualificationAssignmentStore,
    var cache: TtlCache<QualificationAssignment>,
    var hits: Int,
    var misses: Int,
) : QualificationAssignmentStore {
    override fun find(code: String): QualificationAssignment? {
        val hit = (cache.get(code) as QualificationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationAssignment> = inner.list()
}

fun newCachedQualificationAssignmentStore(inner: QualificationAssignmentStore, ttl: Int, clock: Clock): QualificationAssignmentStore {
    return CachedQualificationAssignmentStore(
        inner = inner,
        cache = newTtlCache<QualificationAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification assignments. Empty fields match all. */
data class QualificationAssignmentQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<QualificationAssignment>): MutableList<QualificationAssignment> {
        val matched = filter(items) { candidate: QualificationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationAssignments(store: QualificationAssignmentStore, query: QualificationAssignmentQuery): MutableList<QualificationAssignment> {
    return query.apply(store.list())
}
