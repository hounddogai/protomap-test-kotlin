package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted incident batches by code. */
interface WeightedIncidentBatchStore {
    fun find(code: String): WeightedIncidentBatch?
    fun save(item: WeightedIncidentBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedIncidentBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedIncidentBatchStore(
    var items: MutableMap<String, WeightedIncidentBatch>,
    var order: MutableList<String>,
) : WeightedIncidentBatchStore {
    override fun find(code: String): WeightedIncidentBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedIncidentBatch) {
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

    override fun list(): MutableList<WeightedIncidentBatch> {
        val out = mutableListOf<WeightedIncidentBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedIncidentBatchStore(): WeightedIncidentBatchStore {
    return MemoryWeightedIncidentBatchStore(
        mutableMapOf<String, WeightedIncidentBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedIncidentBatchStore(
    var inner: WeightedIncidentBatchStore,
    var cache: TtlCache<WeightedIncidentBatch>,
    var hits: Int,
    var misses: Int,
) : WeightedIncidentBatchStore {
    override fun find(code: String): WeightedIncidentBatch? {
        val hit = (cache.get(code) as WeightedIncidentBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedIncidentBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedIncidentBatch> = inner.list()
}

fun newCachedWeightedIncidentBatchStore(inner: WeightedIncidentBatchStore, ttl: Int, clock: Clock): WeightedIncidentBatchStore {
    return CachedWeightedIncidentBatchStore(
        inner = inner,
        cache = newTtlCache<WeightedIncidentBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted incident batches. Empty fields match all. */
data class WeightedIncidentBatchQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedIncidentBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedIncidentBatch>): MutableList<WeightedIncidentBatch> {
        val matched = filter(items) { candidate: WeightedIncidentBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedIncidentBatches(store: WeightedIncidentBatchStore, query: WeightedIncidentBatchQuery): MutableList<WeightedIncidentBatch> {
    return query.apply(store.list())
}
