package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive broker assignments by code. */
interface AdaptiveBrokerAssignmentStore {
    fun find(code: String): AdaptiveBrokerAssignment?
    fun save(item: AdaptiveBrokerAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBrokerAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBrokerAssignmentStore(
    var items: MutableMap<String, AdaptiveBrokerAssignment>,
    var order: MutableList<String>,
) : AdaptiveBrokerAssignmentStore {
    override fun find(code: String): AdaptiveBrokerAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBrokerAssignment) {
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

    override fun list(): MutableList<AdaptiveBrokerAssignment> {
        val out = mutableListOf<AdaptiveBrokerAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBrokerAssignmentStore(): AdaptiveBrokerAssignmentStore {
    return MemoryAdaptiveBrokerAssignmentStore(
        mutableMapOf<String, AdaptiveBrokerAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBrokerAssignmentStore(
    var inner: AdaptiveBrokerAssignmentStore,
    var cache: TtlCache<AdaptiveBrokerAssignment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBrokerAssignmentStore {
    override fun find(code: String): AdaptiveBrokerAssignment? {
        val hit = (cache.get(code) as AdaptiveBrokerAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBrokerAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBrokerAssignment> = inner.list()
}

fun newCachedAdaptiveBrokerAssignmentStore(inner: AdaptiveBrokerAssignmentStore, ttl: Int, clock: Clock): AdaptiveBrokerAssignmentStore {
    return CachedAdaptiveBrokerAssignmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBrokerAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive broker assignments. Empty fields match all. */
data class AdaptiveBrokerAssignmentQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBrokerAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveBrokerAssignment>): MutableList<AdaptiveBrokerAssignment> {
        val matched = filter(items) { candidate: AdaptiveBrokerAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBrokerAssignments(store: AdaptiveBrokerAssignmentStore, query: AdaptiveBrokerAssignmentQuery): MutableList<AdaptiveBrokerAssignment> {
    return query.apply(store.list())
}
