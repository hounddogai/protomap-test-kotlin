package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan batches by code. */
interface ServicePlanBatchStore {
    fun find(code: String): ServicePlanBatch?
    fun save(item: ServicePlanBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanBatchStore(
    var items: MutableMap<String, ServicePlanBatch>,
    var order: MutableList<String>,
) : ServicePlanBatchStore {
    override fun find(code: String): ServicePlanBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanBatch) {
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

    override fun list(): MutableList<ServicePlanBatch> {
        val out = mutableListOf<ServicePlanBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanBatchStore(): ServicePlanBatchStore {
    return MemoryServicePlanBatchStore(mutableMapOf<String, ServicePlanBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanBatchStore(
    var inner: ServicePlanBatchStore,
    var cache: TtlCache<ServicePlanBatch>,
    var hits: Int,
    var misses: Int,
) : ServicePlanBatchStore {
    override fun find(code: String): ServicePlanBatch? {
        val hit = (cache.get(code) as ServicePlanBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanBatch> = inner.list()
}

fun newCachedServicePlanBatchStore(inner: ServicePlanBatchStore, ttl: Int, clock: Clock): ServicePlanBatchStore {
    return CachedServicePlanBatchStore(
        inner = inner,
        cache = newTtlCache<ServicePlanBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan batches. Empty fields match all. */
data class ServicePlanBatchQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanBatch>): MutableList<ServicePlanBatch> {
        val matched = filter(items) { candidate: ServicePlanBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanBatches(store: ServicePlanBatchStore, query: ServicePlanBatchQuery): MutableList<ServicePlanBatch> {
    return query.apply(store.list())
}
