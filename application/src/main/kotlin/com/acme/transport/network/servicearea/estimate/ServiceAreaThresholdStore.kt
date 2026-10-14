package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area thresholds by code. */
interface ServiceAreaThresholdStore {
    fun find(code: String): ServiceAreaThreshold?
    fun save(item: ServiceAreaThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaThresholdStore(
    var items: MutableMap<String, ServiceAreaThreshold>,
    var order: MutableList<String>,
) : ServiceAreaThresholdStore {
    override fun find(code: String): ServiceAreaThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaThreshold) {
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

    override fun list(): MutableList<ServiceAreaThreshold> {
        val out = mutableListOf<ServiceAreaThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaThresholdStore(): ServiceAreaThresholdStore {
    return MemoryServiceAreaThresholdStore(
        mutableMapOf<String, ServiceAreaThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaThresholdStore(
    var inner: ServiceAreaThresholdStore,
    var cache: TtlCache<ServiceAreaThreshold>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaThresholdStore {
    override fun find(code: String): ServiceAreaThreshold? {
        val hit = (cache.get(code) as ServiceAreaThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaThreshold> = inner.list()
}

fun newCachedServiceAreaThresholdStore(inner: ServiceAreaThresholdStore, ttl: Int, clock: Clock): ServiceAreaThresholdStore {
    return CachedServiceAreaThresholdStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area thresholds. Empty fields match all. */
data class ServiceAreaThresholdQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServiceAreaThreshold>): MutableList<ServiceAreaThreshold> {
        val matched = filter(items) { candidate: ServiceAreaThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaThresholds(store: ServiceAreaThresholdStore, query: ServiceAreaThresholdQuery): MutableList<ServiceAreaThreshold> {
    return query.apply(store.list())
}
