package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan slots by code. */
interface ServicePlanSlotStore {
    fun find(code: String): ServicePlanSlot?
    fun save(item: ServicePlanSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanSlotStore(
    var items: MutableMap<String, ServicePlanSlot>,
    var order: MutableList<String>,
) : ServicePlanSlotStore {
    override fun find(code: String): ServicePlanSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanSlot) {
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

    override fun list(): MutableList<ServicePlanSlot> {
        val out = mutableListOf<ServicePlanSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanSlotStore(): ServicePlanSlotStore {
    return MemoryServicePlanSlotStore(mutableMapOf<String, ServicePlanSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanSlotStore(
    var inner: ServicePlanSlotStore,
    var cache: TtlCache<ServicePlanSlot>,
    var hits: Int,
    var misses: Int,
) : ServicePlanSlotStore {
    override fun find(code: String): ServicePlanSlot? {
        val hit = (cache.get(code) as ServicePlanSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanSlot> = inner.list()
}

fun newCachedServicePlanSlotStore(inner: ServicePlanSlotStore, ttl: Int, clock: Clock): ServicePlanSlotStore {
    return CachedServicePlanSlotStore(
        inner = inner,
        cache = newTtlCache<ServicePlanSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan slots. Empty fields match all. */
data class ServicePlanSlotQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanSlot>): MutableList<ServicePlanSlot> {
        val matched = filter(items) { candidate: ServicePlanSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanSlots(store: ServicePlanSlotStore, query: ServicePlanSlotQuery): MutableList<ServicePlanSlot> {
    return query.apply(store.list())
}
