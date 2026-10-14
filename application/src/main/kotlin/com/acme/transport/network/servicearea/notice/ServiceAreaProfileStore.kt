package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area profiles by code. */
interface ServiceAreaProfileStore {
    fun find(code: String): ServiceAreaProfile?
    fun save(item: ServiceAreaProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaProfileStore(
    var items: MutableMap<String, ServiceAreaProfile>,
    var order: MutableList<String>,
) : ServiceAreaProfileStore {
    override fun find(code: String): ServiceAreaProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaProfile) {
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

    override fun list(): MutableList<ServiceAreaProfile> {
        val out = mutableListOf<ServiceAreaProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaProfileStore(): ServiceAreaProfileStore {
    return MemoryServiceAreaProfileStore(mutableMapOf<String, ServiceAreaProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaProfileStore(
    var inner: ServiceAreaProfileStore,
    var cache: TtlCache<ServiceAreaProfile>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaProfileStore {
    override fun find(code: String): ServiceAreaProfile? {
        val hit = (cache.get(code) as ServiceAreaProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaProfile> = inner.list()
}

fun newCachedServiceAreaProfileStore(inner: ServiceAreaProfileStore, ttl: Int, clock: Clock): ServiceAreaProfileStore {
    return CachedServiceAreaProfileStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area profiles. Empty fields match all. */
data class ServiceAreaProfileQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ServiceAreaProfile>): MutableList<ServiceAreaProfile> {
        val matched = filter(items) { candidate: ServiceAreaProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaProfiles(store: ServiceAreaProfileStore, query: ServiceAreaProfileQuery): MutableList<ServiceAreaProfile> {
    return query.apply(store.list())
}
