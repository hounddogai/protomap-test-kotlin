package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area adjustments by code. */
interface ServiceAreaAdjustmentStore {
    fun find(code: String): ServiceAreaAdjustment?
    fun save(item: ServiceAreaAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaAdjustmentStore(
    var items: MutableMap<String, ServiceAreaAdjustment>,
    var order: MutableList<String>,
) : ServiceAreaAdjustmentStore {
    override fun find(code: String): ServiceAreaAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaAdjustment) {
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

    override fun list(): MutableList<ServiceAreaAdjustment> {
        val out = mutableListOf<ServiceAreaAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaAdjustmentStore(): ServiceAreaAdjustmentStore {
    return MemoryServiceAreaAdjustmentStore(
        mutableMapOf<String, ServiceAreaAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaAdjustmentStore(
    var inner: ServiceAreaAdjustmentStore,
    var cache: TtlCache<ServiceAreaAdjustment>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaAdjustmentStore {
    override fun find(code: String): ServiceAreaAdjustment? {
        val hit = (cache.get(code) as ServiceAreaAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaAdjustment> = inner.list()
}

fun newCachedServiceAreaAdjustmentStore(inner: ServiceAreaAdjustmentStore, ttl: Int, clock: Clock): ServiceAreaAdjustmentStore {
    return CachedServiceAreaAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area adjustments. Empty fields match all. */
data class ServiceAreaAdjustmentQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ServiceAreaAdjustment>): MutableList<ServiceAreaAdjustment> {
        val matched = filter(items) { candidate: ServiceAreaAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaAdjustments(store: ServiceAreaAdjustmentStore, query: ServiceAreaAdjustmentQuery): MutableList<ServiceAreaAdjustment> {
    return query.apply(store.list())
}
