package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan profiles by code. */
interface ServicePlanProfileStore {
    fun find(code: String): ServicePlanProfile?
    fun save(item: ServicePlanProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanProfileStore(
    var items: MutableMap<String, ServicePlanProfile>,
    var order: MutableList<String>,
) : ServicePlanProfileStore {
    override fun find(code: String): ServicePlanProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanProfile) {
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

    override fun list(): MutableList<ServicePlanProfile> {
        val out = mutableListOf<ServicePlanProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanProfileStore(): ServicePlanProfileStore {
    return MemoryServicePlanProfileStore(mutableMapOf<String, ServicePlanProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanProfileStore(
    var inner: ServicePlanProfileStore,
    var cache: TtlCache<ServicePlanProfile>,
    var hits: Int,
    var misses: Int,
) : ServicePlanProfileStore {
    override fun find(code: String): ServicePlanProfile? {
        val hit = (cache.get(code) as ServicePlanProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanProfile> = inner.list()
}

fun newCachedServicePlanProfileStore(inner: ServicePlanProfileStore, ttl: Int, clock: Clock): ServicePlanProfileStore {
    return CachedServicePlanProfileStore(
        inner = inner,
        cache = newTtlCache<ServicePlanProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan profiles. Empty fields match all. */
data class ServicePlanProfileQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanProfile>): MutableList<ServicePlanProfile> {
        val matched = filter(items) { candidate: ServicePlanProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanProfiles(store: ServicePlanProfileStore, query: ServicePlanProfileQuery): MutableList<ServicePlanProfile> {
    return query.apply(store.list())
}
