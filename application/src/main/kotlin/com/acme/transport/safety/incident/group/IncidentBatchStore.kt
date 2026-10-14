package com.acme.transport.safety.incident.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident batches by code. */
interface IncidentBatchStore {
    fun find(code: String): IncidentBatch?
    fun save(item: IncidentBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentBatchStore(
    var items: MutableMap<String, IncidentBatch>,
    var order: MutableList<String>,
) : IncidentBatchStore {
    override fun find(code: String): IncidentBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentBatch) {
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

    override fun list(): MutableList<IncidentBatch> {
        val out = mutableListOf<IncidentBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentBatchStore(): IncidentBatchStore {
    return MemoryIncidentBatchStore(mutableMapOf<String, IncidentBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentBatchStore(
    var inner: IncidentBatchStore,
    var cache: TtlCache<IncidentBatch>,
    var hits: Int,
    var misses: Int,
) : IncidentBatchStore {
    override fun find(code: String): IncidentBatch? {
        val hit = (cache.get(code) as IncidentBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentBatch> = inner.list()
}

fun newCachedIncidentBatchStore(inner: IncidentBatchStore, ttl: Int, clock: Clock): IncidentBatchStore {
    return CachedIncidentBatchStore(
        inner = inner,
        cache = newTtlCache<IncidentBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident batches. Empty fields match all. */
data class IncidentBatchQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentBatch>): MutableList<IncidentBatch> {
        val matched = filter(items) { candidate: IncidentBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentBatches(store: IncidentBatchStore, query: IncidentBatchQuery): MutableList<IncidentBatch> {
    return query.apply(store.list())
}
