package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive qualification assignments by code. */
interface AdaptiveQualificationAssignmentStore {
    fun find(code: String): AdaptiveQualificationAssignment?
    fun save(item: AdaptiveQualificationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveQualificationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveQualificationAssignmentStore(
    var items: MutableMap<String, AdaptiveQualificationAssignment>,
    var order: MutableList<String>,
) : AdaptiveQualificationAssignmentStore {
    override fun find(code: String): AdaptiveQualificationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveQualificationAssignment) {
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

    override fun list(): MutableList<AdaptiveQualificationAssignment> {
        val out = mutableListOf<AdaptiveQualificationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveQualificationAssignmentStore(): AdaptiveQualificationAssignmentStore {
    return MemoryAdaptiveQualificationAssignmentStore(
        mutableMapOf<String, AdaptiveQualificationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveQualificationAssignmentStore(
    var inner: AdaptiveQualificationAssignmentStore,
    var cache: TtlCache<AdaptiveQualificationAssignment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveQualificationAssignmentStore {
    override fun find(code: String): AdaptiveQualificationAssignment? {
        val hit = (cache.get(code) as AdaptiveQualificationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveQualificationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveQualificationAssignment> = inner.list()
}

fun newCachedAdaptiveQualificationAssignmentStore(inner: AdaptiveQualificationAssignmentStore, ttl: Int, clock: Clock): AdaptiveQualificationAssignmentStore {
    return CachedAdaptiveQualificationAssignmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveQualificationAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive qualification assignments. Empty fields match all. */
data class AdaptiveQualificationAssignmentQuery(
    var locale: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveQualificationAssignment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveQualificationAssignment>): MutableList<AdaptiveQualificationAssignment> {
        val matched = filter(items) { candidate: AdaptiveQualificationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveQualificationAssignments(store: AdaptiveQualificationAssignmentStore, query: AdaptiveQualificationAssignmentQuery): MutableList<AdaptiveQualificationAssignment> {
    return query.apply(store.list())
}
