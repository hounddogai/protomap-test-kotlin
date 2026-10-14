package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation segments by code. */
interface ViolationSegmentStore {
    fun find(code: String): ViolationSegment?
    fun save(item: ViolationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationSegmentStore(
    var items: MutableMap<String, ViolationSegment>,
    var order: MutableList<String>,
) : ViolationSegmentStore {
    override fun find(code: String): ViolationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationSegment) {
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

    override fun list(): MutableList<ViolationSegment> {
        val out = mutableListOf<ViolationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationSegmentStore(): ViolationSegmentStore {
    return MemoryViolationSegmentStore(mutableMapOf<String, ViolationSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationSegmentStore(
    var inner: ViolationSegmentStore,
    var cache: TtlCache<ViolationSegment>,
    var hits: Int,
    var misses: Int,
) : ViolationSegmentStore {
    override fun find(code: String): ViolationSegment? {
        val hit = (cache.get(code) as ViolationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationSegment> = inner.list()
}

fun newCachedViolationSegmentStore(inner: ViolationSegmentStore, ttl: Int, clock: Clock): ViolationSegmentStore {
    return CachedViolationSegmentStore(
        inner = inner,
        cache = newTtlCache<ViolationSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation segments. Empty fields match all. */
data class ViolationSegmentQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationSegment>): MutableList<ViolationSegment> {
        val matched = filter(items) { candidate: ViolationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationSegments(store: ViolationSegmentStore, query: ViolationSegmentQuery): MutableList<ViolationSegment> {
    return query.apply(store.list())
}
