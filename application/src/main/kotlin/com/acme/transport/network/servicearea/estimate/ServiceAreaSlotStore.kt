package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area slots by code. */
interface ServiceAreaSlotStore {
    fun find(code: String): ServiceAreaSlot?
    fun save(item: ServiceAreaSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaSlotStore(
    var items: MutableMap<String, ServiceAreaSlot>,
    var order: MutableList<String>,
) : ServiceAreaSlotStore {
    override fun find(code: String): ServiceAreaSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaSlot) {
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

    override fun list(): MutableList<ServiceAreaSlot> {
        val out = mutableListOf<ServiceAreaSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaSlotStore(): ServiceAreaSlotStore {
    return MemoryServiceAreaSlotStore(mutableMapOf<String, ServiceAreaSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaSlotStore(
    var inner: ServiceAreaSlotStore,
    var cache: TtlCache<ServiceAreaSlot>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaSlotStore {
    override fun find(code: String): ServiceAreaSlot? {
        val hit = (cache.get(code) as ServiceAreaSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaSlot> = inner.list()
}

fun newCachedServiceAreaSlotStore(inner: ServiceAreaSlotStore, ttl: Int, clock: Clock): ServiceAreaSlotStore {
    return CachedServiceAreaSlotStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area slots. Empty fields match all. */
data class ServiceAreaSlotQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaSlot>): MutableList<ServiceAreaSlot> {
        val matched = filter(items) { candidate: ServiceAreaSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaSlots(store: ServiceAreaSlotStore, query: ServiceAreaSlotQuery): MutableList<ServiceAreaSlot> {
    return query.apply(store.list())
}
