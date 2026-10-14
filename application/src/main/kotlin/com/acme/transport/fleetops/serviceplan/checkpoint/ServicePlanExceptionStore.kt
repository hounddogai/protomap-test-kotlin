package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan exceptions by code. */
interface ServicePlanExceptionStore {
    fun find(code: String): ServicePlanException?
    fun save(item: ServicePlanException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanExceptionStore(
    var items: MutableMap<String, ServicePlanException>,
    var order: MutableList<String>,
) : ServicePlanExceptionStore {
    override fun find(code: String): ServicePlanException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanException) {
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

    override fun list(): MutableList<ServicePlanException> {
        val out = mutableListOf<ServicePlanException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanExceptionStore(): ServicePlanExceptionStore {
    return MemoryServicePlanExceptionStore(
        mutableMapOf<String, ServicePlanException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanExceptionStore(
    var inner: ServicePlanExceptionStore,
    var cache: TtlCache<ServicePlanException>,
    var hits: Int,
    var misses: Int,
) : ServicePlanExceptionStore {
    override fun find(code: String): ServicePlanException? {
        val hit = (cache.get(code) as ServicePlanException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanException> = inner.list()
}

fun newCachedServicePlanExceptionStore(inner: ServicePlanExceptionStore, ttl: Int, clock: Clock): ServicePlanExceptionStore {
    return CachedServicePlanExceptionStore(
        inner = inner,
        cache = newTtlCache<ServicePlanException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan exceptions. Empty fields match all. */
data class ServicePlanExceptionQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanException>): MutableList<ServicePlanException> {
        val matched = filter(items) { candidate: ServicePlanException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanExceptions(store: ServicePlanExceptionStore, query: ServicePlanExceptionQuery): MutableList<ServicePlanException> {
    return query.apply(store.list())
}
