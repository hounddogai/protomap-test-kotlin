package com.acme.transport.customs.broker.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker segments by code. */
interface BrokerSegmentStore {
    fun find(code: String): BrokerSegment?
    fun save(item: BrokerSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerSegmentStore(
    var items: MutableMap<String, BrokerSegment>,
    var order: MutableList<String>,
) : BrokerSegmentStore {
    override fun find(code: String): BrokerSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerSegment) {
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

    override fun list(): MutableList<BrokerSegment> {
        val out = mutableListOf<BrokerSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerSegmentStore(): BrokerSegmentStore {
    return MemoryBrokerSegmentStore(mutableMapOf<String, BrokerSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerSegmentStore(
    var inner: BrokerSegmentStore,
    var cache: TtlCache<BrokerSegment>,
    var hits: Int,
    var misses: Int,
) : BrokerSegmentStore {
    override fun find(code: String): BrokerSegment? {
        val hit = (cache.get(code) as BrokerSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerSegment> = inner.list()
}

fun newCachedBrokerSegmentStore(inner: BrokerSegmentStore, ttl: Int, clock: Clock): BrokerSegmentStore {
    return CachedBrokerSegmentStore(
        inner = inner,
        cache = newTtlCache<BrokerSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker segments. Empty fields match all. */
data class BrokerSegmentQuery(
    var region: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerSegment>): MutableList<BrokerSegment> {
        val matched = filter(items) { candidate: BrokerSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerSegments(store: BrokerSegmentStore, query: BrokerSegmentQuery): MutableList<BrokerSegment> {
    return query.apply(store.list())
}
