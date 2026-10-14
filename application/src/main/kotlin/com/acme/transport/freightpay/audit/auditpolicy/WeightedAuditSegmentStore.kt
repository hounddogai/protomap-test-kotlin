package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted audit segments by code. */
interface WeightedAuditSegmentStore {
    fun find(code: String): WeightedAuditSegment?
    fun save(item: WeightedAuditSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAuditSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAuditSegmentStore(
    var items: MutableMap<String, WeightedAuditSegment>,
    var order: MutableList<String>,
) : WeightedAuditSegmentStore {
    override fun find(code: String): WeightedAuditSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAuditSegment) {
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

    override fun list(): MutableList<WeightedAuditSegment> {
        val out = mutableListOf<WeightedAuditSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAuditSegmentStore(): WeightedAuditSegmentStore {
    return MemoryWeightedAuditSegmentStore(
        mutableMapOf<String, WeightedAuditSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAuditSegmentStore(
    var inner: WeightedAuditSegmentStore,
    var cache: TtlCache<WeightedAuditSegment>,
    var hits: Int,
    var misses: Int,
) : WeightedAuditSegmentStore {
    override fun find(code: String): WeightedAuditSegment? {
        val hit = (cache.get(code) as WeightedAuditSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAuditSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAuditSegment> = inner.list()
}

fun newCachedWeightedAuditSegmentStore(inner: WeightedAuditSegmentStore, ttl: Int, clock: Clock): WeightedAuditSegmentStore {
    return CachedWeightedAuditSegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedAuditSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted audit segments. Empty fields match all. */
data class WeightedAuditSegmentQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAuditSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAuditSegment>): MutableList<WeightedAuditSegment> {
        val matched = filter(items) { candidate: WeightedAuditSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAuditSegments(store: WeightedAuditSegmentStore, query: WeightedAuditSegmentQuery): MutableList<WeightedAuditSegment> {
    return query.apply(store.list())
}
