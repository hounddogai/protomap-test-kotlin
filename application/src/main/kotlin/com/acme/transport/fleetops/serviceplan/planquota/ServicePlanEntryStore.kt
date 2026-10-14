package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan entries by code. */
interface ServicePlanEntryStore {
    fun find(code: String): ServicePlanEntry?
    fun save(item: ServicePlanEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanEntryStore(
    var items: MutableMap<String, ServicePlanEntry>,
    var order: MutableList<String>,
) : ServicePlanEntryStore {
    override fun find(code: String): ServicePlanEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanEntry) {
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

    override fun list(): MutableList<ServicePlanEntry> {
        val out = mutableListOf<ServicePlanEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanEntryStore(): ServicePlanEntryStore {
    return MemoryServicePlanEntryStore(mutableMapOf<String, ServicePlanEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanEntryStore(
    var inner: ServicePlanEntryStore,
    var cache: TtlCache<ServicePlanEntry>,
    var hits: Int,
    var misses: Int,
) : ServicePlanEntryStore {
    override fun find(code: String): ServicePlanEntry? {
        val hit = (cache.get(code) as ServicePlanEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanEntry> = inner.list()
}

fun newCachedServicePlanEntryStore(inner: ServicePlanEntryStore, ttl: Int, clock: Clock): ServicePlanEntryStore {
    return CachedServicePlanEntryStore(
        inner = inner,
        cache = newTtlCache<ServicePlanEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan entries. Empty fields match all. */
data class ServicePlanEntryQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanEntry): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanEntry>): MutableList<ServicePlanEntry> {
        val matched = filter(items) { candidate: ServicePlanEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanEntries(store: ServicePlanEntryStore, query: ServicePlanEntryQuery): MutableList<ServicePlanEntry> {
    return query.apply(store.list())
}
