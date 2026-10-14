package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan segments by code. */
interface ManualServicePlanSegmentStore {
    fun find(code: String): ManualServicePlanSegment?
    fun save(item: ManualServicePlanSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanSegmentStore(
    var items: MutableMap<String, ManualServicePlanSegment>,
    var order: MutableList<String>,
) : ManualServicePlanSegmentStore {
    override fun find(code: String): ManualServicePlanSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanSegment) {
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

    override fun list(): MutableList<ManualServicePlanSegment> {
        val out = mutableListOf<ManualServicePlanSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanSegmentStore(): ManualServicePlanSegmentStore {
    return MemoryManualServicePlanSegmentStore(
        mutableMapOf<String, ManualServicePlanSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanSegmentStore(
    var inner: ManualServicePlanSegmentStore,
    var cache: TtlCache<ManualServicePlanSegment>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanSegmentStore {
    override fun find(code: String): ManualServicePlanSegment? {
        val hit = (cache.get(code) as ManualServicePlanSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanSegment> = inner.list()
}

fun newCachedManualServicePlanSegmentStore(inner: ManualServicePlanSegmentStore, ttl: Int, clock: Clock): ManualServicePlanSegmentStore {
    return CachedManualServicePlanSegmentStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan segments. Empty fields match all. */
data class ManualServicePlanSegmentQuery(
    var locale: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanSegment>): MutableList<ManualServicePlanSegment> {
        val matched = filter(items) { candidate: ManualServicePlanSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanSegments(store: ManualServicePlanSegmentStore, query: ManualServicePlanSegmentQuery): MutableList<ManualServicePlanSegment> {
    return query.apply(store.list())
}
