package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area cycles by code. */
interface ServiceAreaCycleStore {
    fun find(code: String): ServiceAreaCycle?
    fun save(item: ServiceAreaCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaCycleStore(
    var items: MutableMap<String, ServiceAreaCycle>,
    var order: MutableList<String>,
) : ServiceAreaCycleStore {
    override fun find(code: String): ServiceAreaCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaCycle) {
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

    override fun list(): MutableList<ServiceAreaCycle> {
        val out = mutableListOf<ServiceAreaCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaCycleStore(): ServiceAreaCycleStore {
    return MemoryServiceAreaCycleStore(mutableMapOf<String, ServiceAreaCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaCycleStore(
    var inner: ServiceAreaCycleStore,
    var cache: TtlCache<ServiceAreaCycle>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaCycleStore {
    override fun find(code: String): ServiceAreaCycle? {
        val hit = (cache.get(code) as ServiceAreaCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaCycle> = inner.list()
}

fun newCachedServiceAreaCycleStore(inner: ServiceAreaCycleStore, ttl: Int, clock: Clock): ServiceAreaCycleStore {
    return CachedServiceAreaCycleStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area cycles. Empty fields match all. */
data class ServiceAreaCycleQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServiceAreaCycle>): MutableList<ServiceAreaCycle> {
        val matched = filter(items) { candidate: ServiceAreaCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaCycles(store: ServiceAreaCycleStore, query: ServiceAreaCycleQuery): MutableList<ServiceAreaCycle> {
    return query.apply(store.list())
}
