package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss segments by code. */
interface NearMissSegmentStore {
    fun find(code: String): NearMissSegment?
    fun save(item: NearMissSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissSegmentStore(
    var items: MutableMap<String, NearMissSegment>,
    var order: MutableList<String>,
) : NearMissSegmentStore {
    override fun find(code: String): NearMissSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissSegment) {
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

    override fun list(): MutableList<NearMissSegment> {
        val out = mutableListOf<NearMissSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissSegmentStore(): NearMissSegmentStore {
    return MemoryNearMissSegmentStore(mutableMapOf<String, NearMissSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissSegmentStore(
    var inner: NearMissSegmentStore,
    var cache: TtlCache<NearMissSegment>,
    var hits: Int,
    var misses: Int,
) : NearMissSegmentStore {
    override fun find(code: String): NearMissSegment? {
        val hit = (cache.get(code) as NearMissSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissSegment> = inner.list()
}

fun newCachedNearMissSegmentStore(inner: NearMissSegmentStore, ttl: Int, clock: Clock): NearMissSegmentStore {
    return CachedNearMissSegmentStore(
        inner = inner,
        cache = newTtlCache<NearMissSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss segments. Empty fields match all. */
data class NearMissSegmentQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissSegment>): MutableList<NearMissSegment> {
        val matched = filter(items) { candidate: NearMissSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissSegments(store: NearMissSegmentStore, query: NearMissSegmentQuery): MutableList<NearMissSegment> {
    return query.apply(store.list())
}
