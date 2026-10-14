package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default tender segments by code. */
interface DefaultTenderSegmentStore {
    fun find(code: String): DefaultTenderSegment?
    fun save(item: DefaultTenderSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTenderSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTenderSegmentStore(
    var items: MutableMap<String, DefaultTenderSegment>,
    var order: MutableList<String>,
) : DefaultTenderSegmentStore {
    override fun find(code: String): DefaultTenderSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTenderSegment) {
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

    override fun list(): MutableList<DefaultTenderSegment> {
        val out = mutableListOf<DefaultTenderSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTenderSegmentStore(): DefaultTenderSegmentStore {
    return MemoryDefaultTenderSegmentStore(
        mutableMapOf<String, DefaultTenderSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTenderSegmentStore(
    var inner: DefaultTenderSegmentStore,
    var cache: TtlCache<DefaultTenderSegment>,
    var hits: Int,
    var misses: Int,
) : DefaultTenderSegmentStore {
    override fun find(code: String): DefaultTenderSegment? {
        val hit = (cache.get(code) as DefaultTenderSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTenderSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTenderSegment> = inner.list()
}

fun newCachedDefaultTenderSegmentStore(inner: DefaultTenderSegmentStore, ttl: Int, clock: Clock): DefaultTenderSegmentStore {
    return CachedDefaultTenderSegmentStore(
        inner = inner,
        cache = newTtlCache<DefaultTenderSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tender segments. Empty fields match all. */
data class DefaultTenderSegmentQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTenderSegment): Boolean {
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

    fun apply(items: MutableList<DefaultTenderSegment>): MutableList<DefaultTenderSegment> {
        val matched = filter(items) { candidate: DefaultTenderSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTenderSegments(store: DefaultTenderSegmentStore, query: DefaultTenderSegmentQuery): MutableList<DefaultTenderSegment> {
    return query.apply(store.list())
}
