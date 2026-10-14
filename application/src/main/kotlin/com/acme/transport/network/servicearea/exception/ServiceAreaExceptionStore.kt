package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area exceptions by code. */
interface ServiceAreaExceptionStore {
    fun find(code: String): ServiceAreaException?
    fun save(item: ServiceAreaException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaExceptionStore(
    var items: MutableMap<String, ServiceAreaException>,
    var order: MutableList<String>,
) : ServiceAreaExceptionStore {
    override fun find(code: String): ServiceAreaException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaException) {
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

    override fun list(): MutableList<ServiceAreaException> {
        val out = mutableListOf<ServiceAreaException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaExceptionStore(): ServiceAreaExceptionStore {
    return MemoryServiceAreaExceptionStore(
        mutableMapOf<String, ServiceAreaException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaExceptionStore(
    var inner: ServiceAreaExceptionStore,
    var cache: TtlCache<ServiceAreaException>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaExceptionStore {
    override fun find(code: String): ServiceAreaException? {
        val hit = (cache.get(code) as ServiceAreaException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaException> = inner.list()
}

fun newCachedServiceAreaExceptionStore(inner: ServiceAreaExceptionStore, ttl: Int, clock: Clock): ServiceAreaExceptionStore {
    return CachedServiceAreaExceptionStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area exceptions. Empty fields match all. */
data class ServiceAreaExceptionQuery(
    var region: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaException): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaException>): MutableList<ServiceAreaException> {
        val matched = filter(items) { candidate: ServiceAreaException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaExceptions(store: ServiceAreaExceptionStore, query: ServiceAreaExceptionQuery): MutableList<ServiceAreaException> {
    return query.apply(store.list())
}
