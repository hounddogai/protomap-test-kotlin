package com.acme.transport.emissions.idling.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling idling segments by code. */
interface RollingIdlingSegmentStore {
    fun find(code: String): RollingIdlingSegment?
    fun save(item: RollingIdlingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingIdlingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingIdlingSegmentStore(
    var items: MutableMap<String, RollingIdlingSegment>,
    var order: MutableList<String>,
) : RollingIdlingSegmentStore {
    override fun find(code: String): RollingIdlingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingIdlingSegment) {
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

    override fun list(): MutableList<RollingIdlingSegment> {
        val out = mutableListOf<RollingIdlingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingIdlingSegmentStore(): RollingIdlingSegmentStore {
    return MemoryRollingIdlingSegmentStore(
        mutableMapOf<String, RollingIdlingSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingIdlingSegmentStore(
    var inner: RollingIdlingSegmentStore,
    var cache: TtlCache<RollingIdlingSegment>,
    var hits: Int,
    var misses: Int,
) : RollingIdlingSegmentStore {
    override fun find(code: String): RollingIdlingSegment? {
        val hit = (cache.get(code) as RollingIdlingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingIdlingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingIdlingSegment> = inner.list()
}

fun newCachedRollingIdlingSegmentStore(inner: RollingIdlingSegmentStore, ttl: Int, clock: Clock): RollingIdlingSegmentStore {
    return CachedRollingIdlingSegmentStore(
        inner = inner,
        cache = newTtlCache<RollingIdlingSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling idling segments. Empty fields match all. */
data class RollingIdlingSegmentQuery(
    var unit: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingIdlingSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingIdlingSegment>): MutableList<RollingIdlingSegment> {
        val matched = filter(items) { candidate: RollingIdlingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingIdlingSegments(store: RollingIdlingSegmentStore, query: RollingIdlingSegmentQuery): MutableList<RollingIdlingSegment> {
    return query.apply(store.list())
}
