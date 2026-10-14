package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area targets by code. */
interface ServiceAreaTargetStore {
    fun find(code: String): ServiceAreaTarget?
    fun save(item: ServiceAreaTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaTargetStore(
    var items: MutableMap<String, ServiceAreaTarget>,
    var order: MutableList<String>,
) : ServiceAreaTargetStore {
    override fun find(code: String): ServiceAreaTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaTarget) {
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

    override fun list(): MutableList<ServiceAreaTarget> {
        val out = mutableListOf<ServiceAreaTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaTargetStore(): ServiceAreaTargetStore {
    return MemoryServiceAreaTargetStore(mutableMapOf<String, ServiceAreaTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaTargetStore(
    var inner: ServiceAreaTargetStore,
    var cache: TtlCache<ServiceAreaTarget>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaTargetStore {
    override fun find(code: String): ServiceAreaTarget? {
        val hit = (cache.get(code) as ServiceAreaTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaTarget> = inner.list()
}

fun newCachedServiceAreaTargetStore(inner: ServiceAreaTargetStore, ttl: Int, clock: Clock): ServiceAreaTargetStore {
    return CachedServiceAreaTargetStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area targets. Empty fields match all. */
data class ServiceAreaTargetQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaTarget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaTarget>): MutableList<ServiceAreaTarget> {
        val matched = filter(items) { candidate: ServiceAreaTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaTargets(store: ServiceAreaTargetStore, query: ServiceAreaTargetQuery): MutableList<ServiceAreaTarget> {
    return query.apply(store.list())
}
