package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute segments by code. */
interface DisputeSegmentStore {
    fun find(code: String): DisputeSegment?
    fun save(item: DisputeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeSegmentStore(
    var items: MutableMap<String, DisputeSegment>,
    var order: MutableList<String>,
) : DisputeSegmentStore {
    override fun find(code: String): DisputeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeSegment) {
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

    override fun list(): MutableList<DisputeSegment> {
        val out = mutableListOf<DisputeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeSegmentStore(): DisputeSegmentStore {
    return MemoryDisputeSegmentStore(mutableMapOf<String, DisputeSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeSegmentStore(
    var inner: DisputeSegmentStore,
    var cache: TtlCache<DisputeSegment>,
    var hits: Int,
    var misses: Int,
) : DisputeSegmentStore {
    override fun find(code: String): DisputeSegment? {
        val hit = (cache.get(code) as DisputeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeSegment> = inner.list()
}

fun newCachedDisputeSegmentStore(inner: DisputeSegmentStore, ttl: Int, clock: Clock): DisputeSegmentStore {
    return CachedDisputeSegmentStore(
        inner = inner,
        cache = newTtlCache<DisputeSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute segments. Empty fields match all. */
data class DisputeSegmentQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeSegment>): MutableList<DisputeSegment> {
        val matched = filter(items) { candidate: DisputeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeSegments(store: DisputeSegmentStore, query: DisputeSegmentQuery): MutableList<DisputeSegment> {
    return query.apply(store.list())
}
