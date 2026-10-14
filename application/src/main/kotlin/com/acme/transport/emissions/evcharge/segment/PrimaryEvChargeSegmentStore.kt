package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary ev charge segments by code. */
interface PrimaryEvChargeSegmentStore {
    fun find(code: String): PrimaryEvChargeSegment?
    fun save(item: PrimaryEvChargeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryEvChargeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryEvChargeSegmentStore(
    var items: MutableMap<String, PrimaryEvChargeSegment>,
    var order: MutableList<String>,
) : PrimaryEvChargeSegmentStore {
    override fun find(code: String): PrimaryEvChargeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryEvChargeSegment) {
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

    override fun list(): MutableList<PrimaryEvChargeSegment> {
        val out = mutableListOf<PrimaryEvChargeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryEvChargeSegmentStore(): PrimaryEvChargeSegmentStore {
    return MemoryPrimaryEvChargeSegmentStore(
        mutableMapOf<String, PrimaryEvChargeSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryEvChargeSegmentStore(
    var inner: PrimaryEvChargeSegmentStore,
    var cache: TtlCache<PrimaryEvChargeSegment>,
    var hits: Int,
    var misses: Int,
) : PrimaryEvChargeSegmentStore {
    override fun find(code: String): PrimaryEvChargeSegment? {
        val hit = (cache.get(code) as PrimaryEvChargeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryEvChargeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryEvChargeSegment> = inner.list()
}

fun newCachedPrimaryEvChargeSegmentStore(inner: PrimaryEvChargeSegmentStore, ttl: Int, clock: Clock): PrimaryEvChargeSegmentStore {
    return CachedPrimaryEvChargeSegmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryEvChargeSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary ev charge segments. Empty fields match all. */
data class PrimaryEvChargeSegmentQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryEvChargeSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryEvChargeSegment>): MutableList<PrimaryEvChargeSegment> {
        val matched = filter(items) { candidate: PrimaryEvChargeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryEvChargeSegments(store: PrimaryEvChargeSegmentStore, query: PrimaryEvChargeSegmentQuery): MutableList<PrimaryEvChargeSegment> {
    return query.apply(store.list())
}
