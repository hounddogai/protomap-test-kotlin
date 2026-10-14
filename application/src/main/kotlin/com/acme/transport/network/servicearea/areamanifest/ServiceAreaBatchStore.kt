package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area batches by code. */
interface ServiceAreaBatchStore {
    fun find(code: String): ServiceAreaBatch?
    fun save(item: ServiceAreaBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaBatchStore(
    var items: MutableMap<String, ServiceAreaBatch>,
    var order: MutableList<String>,
) : ServiceAreaBatchStore {
    override fun find(code: String): ServiceAreaBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaBatch) {
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

    override fun list(): MutableList<ServiceAreaBatch> {
        val out = mutableListOf<ServiceAreaBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaBatchStore(): ServiceAreaBatchStore {
    return MemoryServiceAreaBatchStore(mutableMapOf<String, ServiceAreaBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaBatchStore(
    var inner: ServiceAreaBatchStore,
    var cache: TtlCache<ServiceAreaBatch>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaBatchStore {
    override fun find(code: String): ServiceAreaBatch? {
        val hit = (cache.get(code) as ServiceAreaBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaBatch> = inner.list()
}

fun newCachedServiceAreaBatchStore(inner: ServiceAreaBatchStore, ttl: Int, clock: Clock): ServiceAreaBatchStore {
    return CachedServiceAreaBatchStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area batches. Empty fields match all. */
data class ServiceAreaBatchQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaBatch>): MutableList<ServiceAreaBatch> {
        val matched = filter(items) { candidate: ServiceAreaBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaBatches(store: ServiceAreaBatchStore, query: ServiceAreaBatchQuery): MutableList<ServiceAreaBatch> {
    return query.apply(store.list())
}
