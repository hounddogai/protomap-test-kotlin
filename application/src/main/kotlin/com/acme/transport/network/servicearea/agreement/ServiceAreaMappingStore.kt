package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area mappings by code. */
interface ServiceAreaMappingStore {
    fun find(code: String): ServiceAreaMapping?
    fun save(item: ServiceAreaMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaMappingStore(
    var items: MutableMap<String, ServiceAreaMapping>,
    var order: MutableList<String>,
) : ServiceAreaMappingStore {
    override fun find(code: String): ServiceAreaMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaMapping) {
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

    override fun list(): MutableList<ServiceAreaMapping> {
        val out = mutableListOf<ServiceAreaMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaMappingStore(): ServiceAreaMappingStore {
    return MemoryServiceAreaMappingStore(mutableMapOf<String, ServiceAreaMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaMappingStore(
    var inner: ServiceAreaMappingStore,
    var cache: TtlCache<ServiceAreaMapping>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaMappingStore {
    override fun find(code: String): ServiceAreaMapping? {
        val hit = (cache.get(code) as ServiceAreaMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaMapping> = inner.list()
}

fun newCachedServiceAreaMappingStore(inner: ServiceAreaMappingStore, ttl: Int, clock: Clock): ServiceAreaMappingStore {
    return CachedServiceAreaMappingStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area mappings. Empty fields match all. */
data class ServiceAreaMappingQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaMapping>): MutableList<ServiceAreaMapping> {
        val matched = filter(items) { candidate: ServiceAreaMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaMappings(store: ServiceAreaMappingStore, query: ServiceAreaMappingQuery): MutableList<ServiceAreaMapping> {
    return query.apply(store.list())
}
