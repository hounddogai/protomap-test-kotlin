package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area bundles by code. */
interface ServiceAreaBundleStore {
    fun find(code: String): ServiceAreaBundle?
    fun save(item: ServiceAreaBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaBundleStore(
    var items: MutableMap<String, ServiceAreaBundle>,
    var order: MutableList<String>,
) : ServiceAreaBundleStore {
    override fun find(code: String): ServiceAreaBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaBundle) {
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

    override fun list(): MutableList<ServiceAreaBundle> {
        val out = mutableListOf<ServiceAreaBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaBundleStore(): ServiceAreaBundleStore {
    return MemoryServiceAreaBundleStore(mutableMapOf<String, ServiceAreaBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaBundleStore(
    var inner: ServiceAreaBundleStore,
    var cache: TtlCache<ServiceAreaBundle>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaBundleStore {
    override fun find(code: String): ServiceAreaBundle? {
        val hit = (cache.get(code) as ServiceAreaBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaBundle> = inner.list()
}

fun newCachedServiceAreaBundleStore(inner: ServiceAreaBundleStore, ttl: Int, clock: Clock): ServiceAreaBundleStore {
    return CachedServiceAreaBundleStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area bundles. Empty fields match all. */
data class ServiceAreaBundleQuery(
    var segment: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaBundle>): MutableList<ServiceAreaBundle> {
        val matched = filter(items) { candidate: ServiceAreaBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaBundles(store: ServiceAreaBundleStore, query: ServiceAreaBundleQuery): MutableList<ServiceAreaBundle> {
    return query.apply(store.list())
}
